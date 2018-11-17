from bs4 import BeautifulSoup
import urllib.request
import requests
import pandas as pd
import json
import codecs

titles = []
images = []
prices = []
descriptions = []


def LoadConfig(path):
    a = open(path)
    Config = json.load(a)
    return Config

def LoadWeb(link, css):
    page = urllib.request.urlopen(link)
    soup = BeautifulSoup(page, 'html.parser')
    get_data = soup.find_all(class_=css)
    return get_data

def GetTitle(data_element):
    title = data.select(data_element)
    link = [sd["href"] for sd in title]
    title = [sd.get_text() for sd in title]
    title = "".join(str(x) for x in title)
    link = "".join((str(x) for x in link))
    titles.append(title)
    return titles, link

def GetImage(data_element):
    image = data.select(data_element)
    image = [sd.get('src') for sd in image]
    image = "".join(str(x) for x in image)
    images.append(image)
    return images

def GetPrice(data_element):
    price = data.select(data_element)
    price = [sd.get_text() for sd in price]
    price = "".join(str(x) for x in price)
    prices.append(price)
    return prices

def GetDescription(data_element,content_element):
    a = desc[0].find(itemprop=data_element)
    if a is not None:
        des = a.select(content_element)
        des = [sd.get_text() for sd in des]
        des = "".join(str(x) for x in des)
    else:
        des = 'None'
    descriptions.append(des)
    return descriptions


Config = LoadConfig('WebCrawlerConfig.json')

if Config is not None:
    for x in range(0, len(Config)):
        ReadConfig = Config[x]
        base_url = str(ReadConfig['site']) + "/" + str(ReadConfig['path'])
        print("Your url: %s" % base_url)
        num_page = ReadConfig['num_page']
        print("Total page: %s" % num_page)
        num_page = int(num_page)
        if num_page > 1:
            for i in range(1, num_page + 1):
                url = base_url + "/" + str(i)
                print(url)

                # Get web's tree
                get_data = LoadWeb(url, ReadConfig['key_class'])
                for j in range(0, len(get_data)):
                    data = get_data[j]

                    # GET TITLE
                    tt = GetTitle(ReadConfig['data_element']['title'])
                    # print(titles)

                    # GET IMAGE
                    GetImage(ReadConfig['data_element']['image'])
                    # print(images)

                    # GET PRICE
                    GetPrice(ReadConfig['data_element']['price'])
                    # print(prices)

                    # GET DESCRIPTION
                    desc = LoadWeb(tt[1],ReadConfig['data_element']['description']['key_class'])
                    GetDescription(ReadConfig['data_element']['description']['data_element'],
                                   ReadConfig['data_element']['description']['content'])
                    # print(descriptions)
        else:
            get_data = LoadWeb(base_url, ReadConfig['key_class'])
            for j in range(0, len(get_data)):
                data = get_data[j]

                # GET TITLE
                tt = GetTitle(ReadConfig['data_element']['title'])
                # print(titles)

                # GET IMAGE
                GetImage(ReadConfig['data_element']['image'])
                # print(images)

                # GET PRICE
                GetPrice(ReadConfig['data_element']['price'])
                # print(prices)

                # GET DESCRIPTION
                desc = LoadWeb(tt[1], ReadConfig['data_element']['description']['key_class'])
                GetDescription(ReadConfig['data_element']['description']['data_element'],
                               ReadConfig['data_element']['description']['content'])

                # print(descriptions)

        # Create data as json
        data_frame = ({
            "Title": tt[0],
            "Image": images,
            "Price": prices,
            "Description": descriptions
        })

        # Add data to padas's data frame
        df = pd.DataFrame(data_frame)
        print(df.info())

        # Create csv's file
        df.to_csv('data.csv')
        print(df)


'''
if ReadConfig is not None:
    base_url = str(ReadConfig['site']) + "/" + str(ReadConfig['path']) + "/"
    print("Your url: %s" %base_url)
    num_page = ReadConfig['num_page']
    print("Total page: %s" %num_page)
    for i in range(1, num_page + 1):
        url = base_url + str(i)
        print(url)
        page = urllib.request.urlopen(url)
        soup = BeautifulSoup(page, 'html.parser')
        get_data = soup.find_all(class_=ReadConfig['key_class'])
        for j in range(0, len(get_data)):
            data = get_data[j]

            #GET TITLE
            title = data.select(ReadConfig['data_element']['title'])
            title = [sd.get_text() for sd in title]
            title = "".join(str(x) for x in title)
            titles.append(title)
            #print(titles)

            #GET IMAGE
            image = data.select(ReadConfig['data_element']['image'])
            image = [sd.get('src') for sd in image]
            image = "".join(str(x) for x in image)
            images.append(image)
            #print(images)

            #GET PRICE
            price = data.select(ReadConfig['data_element']['price'])
            price = [sd.get_text() for sd in price]
            price = "".join(str(x) for x in price)
            prices.append(price)
            #print(prices)

            #GET DESCRIPTION
            description = data.select(ReadConfig['data_element']['description'])
            description = [sd["title"] for sd in description]
            description = "".join(str(x) for x in description)
            descriptions.append(description)
            #print(descriptions)

data_frame = ({
    "Title": titles,
    "Image": images,
    "Price": prices,
    "Description": descriptions
})

df = pd.DataFrame(data_frame)
print(df.info())
df.to_csv('data.csv')
print(df)
'''