package Khachhang;



import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
 interface KhachhangRepository extends ElasticsearchRepository<Khachhang, Integer> {

}

