package com.example.hoalan.importJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ImportJson {
    private static final boolean File_Import_Default = false;
    private static final String File_Import_Mode_Default = "transport";
    private static final String Default_File = "json";
    private static final String File_Import_FileText = "fileimport.fileext";
    private static final String File_Import_Type = "fileimport.type";
    private static final String File_Import_Max_Volume = "fileimport.max_volume_per_bulk_request";
    private static final String File_Import_Index = "fileimport.index";
    private static final String File_Import_Max_Concurrent = "fileimport.max_concurrent_bulk_request";
    private static final String File_Import_Max_Bulk_Action = "fileimport.max_bulk_actions";
    private static final String File_Flush_Interval = "fileimport.flush_interval";
    private static final String File_Import_Mode = "fileimport.mode";
    private static final String File_Import_Tranports_Address = "fileimport.transport.address";
    private static final String File_Import_LineByLine = "fileimport.linebyline";
    private static final String File_Import_Root = "fileimport.root";
    private static final Logger loggers = LogManager.getLogger(ImportJson.class);
    private final TimeValue flushInterval;
    private final int maxBulkActions;
    private final int maxConcurrentBulkRequests;
    private final ByteSizeValue MaxVolumePerBulkRequest;
    private final String index;
    private final String type;

    public ImportJson(TimeValue flushInterval, int maxBulkActions, int maxConcurrentBulkRequests, ByteSizeValue maxVolumePerBulkRequest, String index, String type) {
        super();
        this.flushInterval = flushInterval;
        this.maxBulkActions = maxBulkActions;
        this.maxConcurrentBulkRequests = maxConcurrentBulkRequests;
        MaxVolumePerBulkRequest = maxVolumePerBulkRequest;
        this.index = index;
        this.type = type;
        loggers.debug("index: " + index);
        loggers.debug("type: " + type);
        loggers.debug("flushInterval: " + flushInterval);
        loggers.debug("maxBulkActions: " + maxBulkActions);
        loggers.debug("maxConcurrentBulkRequests: " + maxConcurrentBulkRequests);
        loggers.debug("maxVolumePerBulkRequest: " + maxVolumePerBulkRequest);
    }

    public static void main(final String[] args) throws Exception {
        try {
            Settings settings = ImmutableSettings.builder().loadFromClasspath("file_import_settings.yml").build();

            if (settings.names().isEmpty()) {

                if (args == null || args.length == 0) {
                    System.out.println("Usage: de.codecentric.elasticsearch.fileimport.FileImporter <path to config file>");
                    System.exit(-1);
                }

                settings = ImmutableSettings.builder().loadFromSource(new String(Files.readAllBytes(Paths.get(args[0])), "UTF-8")).build();

                if (settings.names().isEmpty()) {
                    System.out.println(args[0] + " contains no settings");
                    System.exit(-1);
                }
            }

            final ImportJson importer = new ImportJson(settings);

            switch (settings.get(File_Import_Mode, File_Import_Mode_Default)) {
                case "node":
                    importer.startAsNode(settings);
                    break;
                default:
                    importer.startAsTransportClient(settings);
                    break;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    public ImportJson(final Settings settings) {
        this(settings.getAsTime(File_Flush_Interval, null), settings.getAsInt(File_Import_Max_Bulk_Action, 1000), settings.getAsInt(
                File_Import_Max_Concurrent, 1), settings.getAsBytesSize(File_Import_Max_Volume,
                ByteSizeValue.parseBytesSizeValue("10MB")), settings.get(File_Import_Index), settings.get(File_Import_Type));
    }

    @SuppressWarnings("resource")
    public int startAsTransportClient(final Settings settings) throws IOException {
        loggers.debug("Connecting as transport client");
        final TransportClient client;
        for (final String transportAddress : settings.getAsArray(FILEIMPORT_TRANSPORT_ADDRESSES)) {
            final String[] hostnamePort = transportAddress.split(":");
            loggers.debug("Added transport endpoint " + hostnamePort[0] + ":" + Integer.parseInt(hostnamePort[1]));
            client.addTransportAddresses(new InetSocketTransportAddress(hostnamePort[0], Integer.parseInt(hostnamePort[1])));
        }

        return start(client, Paths.get(settings.get(FILEIMPORT_ROOT)),
                settings.getAsBoolean(FILEIMPORT_LINEBYLINE, FILEIMPORT_LINEBYLINE_DEFAULT),
                settings.get(FILEIMPORT_FILEEXT, DEFAULT_FILE_EXT));
    }

    public int startAsNode(final Settings settings) throws IOException {
        logger.debug("Connecting as node client");
        final Node node = NodeBuilder.nodeBuilder().settings(settings).client(true).loadConfigSettings(false).local(false).node();
        final Client client = node.client();
        try {
            return start(client, Paths.get(settings.get(FILEIMPORT_ROOT)),
                    settings.getAsBoolean(FILEIMPORT_LINEBYLINE, FILEIMPORT_LINEBYLINE_DEFAULT),
                    settings.get(FILEIMPORT_FILEEXT, DEFAULT_FILE_EXT));
        } finally {
            if (node != null) {
                node.close();
            }
        }
    }

    public int start(final Client client, final Path root, final boolean lineByLine, final String fileExt) throws IOException {
        logger.info("Importing *.{} files from {}", fileExt, root.toAbsolutePath());

        long countBeforeImport = 0;
        try {
            client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
            countBeforeImport = client.count(new CountRequest(index).types(type)).actionGet().getCount();
        } catch (final IndexMissingException e) {
            // ignore
        }

        final BulkListener bulkListener = new BulkListener();
        final BulkProcessor bulk = BulkProcessor.builder(client, bulkListener).setBulkActions(maxBulkActions)
                .setConcurrentRequests(maxConcurrentBulkRequests).setBulkSize(maxVolumePerBulkRequest).setFlushInterval(flushInterval)
                .build();
        final AtomicLong expectedCount = new AtomicLong();
        try {

            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {

                    if (fileExt == null || fileExt.equals("*") || file.toString().endsWith("." + fileExt)) {
                        importFile(bulk, file, lineByLine, expectedCount);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            long count = 0;
            do {
                bulk.flush();

                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                try {
                    client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
                    count = client.count(new CountRequest(index).types(type)).actionGet().getCount();
                } catch (final IndexMissingException e) {
                    // ignore
                }

                if (bulkListener.isErrored()) {
                    logger.error("Error while bulk indexing");
                    break;
                }

                logger.info(count + "/" + (expectedCount.get() - countBeforeImport));
            } while (count < expectedCount.get() - countBeforeImport);

            logger.info("Indexed " + count + " documents");
            return (int) count;
        } catch (final IOException e) {
            throw e;
        } finally {
            bulk.close();
            if (client != null) {
                client.close();
            }
        }
    }

    protected void importFile(final BulkProcessor bulk, final Path file, final boolean lineByLine, final AtomicLong expectedCount)
            throws IOException {

        if (logger.isTraceEnabled()) {
            logger.trace("Import {} as {}", file, lineByLine ? "line-by-line" : "whole file");
        }

        if (lineByLine) {
            try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8).filter(f -> !f.trim().isEmpty())) {
                long lineCount = 0;
                for (final String line : (Iterable<String>) lines::iterator) {
                    final IndexRequest request = Requests.indexRequest(index).type(type).source(line);
                    bulk.add(request);
                    expectedCount.incrementAndGet();
                    if (logger.isTraceEnabled()) {
                        logger.trace("    Line {}", lineCount++);
                    }
                }
            }
        } else {
            final byte[] source = Files.readAllBytes(file);
            final IndexRequest request = Requests.indexRequest(index).type(type).source(source);
            bulk.add(request);
            expectedCount.incrementAndGet();
        }
    }

    private static class BulkListener implements BulkProcessor.Listener {

        private volatile boolean errored;

        public boolean isErrored() {
            return errored;
        }

        @Override
        public void afterBulk(final long executionId, final BulkRequest request, final BulkResponse response) {
            logger.debug("Bulk actions done [{}] [{} items] [{}ms]", executionId, response.getItems().length, response.getTookInMillis());

            for (final BulkItemResponse itemResponse : response.getItems()) {
                if (itemResponse.isFailed()) {
                    logger.error(itemResponse.getFailure().getMessage());
                    errored = true;
                }
            }
        }

        @Override
        public void afterBulk(final long executionId, final BulkRequest request, final Throwable failure) {
            logger.error("Bulk actions done with errors [" + executionId + "] error", failure);
            errored = true;
        }

        @Override
        public void beforeBulk(final long executionId, final BulkRequest request) {
            logger.debug("New bulk actions queued [{}] of [{} items]", executionId, request.numberOfActions());
        }
    };
}
