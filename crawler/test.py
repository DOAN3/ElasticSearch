import json
from bs4 import BeautifulSoup
import urllib.request
prices = []
def LoadConfig(path):
    a = open(path)
    Config = json.load(a)
    return Config
ReadConfig = LoadConfig('WebCrawlerConfig.json')
def sssurl(base_url, css_class):
    page = urllib.request.urlopen(base_url)
    soup = BeautifulSoup(page, 'html.parser')
    get_data = soup.find_all(class_=css_class)
    return get_data

def GetPrice(ad):
    price = data.select(ad)
    price = [sd.get_text() for sd in price]
    price = "".join(str(x) for x in price)
    prices.append(price)
    return prices

base_url = "https://vietflower.vn/hoa-tuoi/lan-ho-diep"
page = urllib.request.urlopen(base_url)
soup = BeautifulSoup(page, 'html.parser')
get_data = soup.find_all(class_="product")
data = get_data[0]

def GetTitle(data_element):
    title = data.select(data_element)
    link = [sd["href"] for sd in title]
    title = [sd.get_text() for sd in title]
    title = "".join(str(x) for x in title)
    link = "".join((str(x) for x in link))
    prices.append(title)
    return prices, link

tt = GetTitle(".mg0 a")
print(tt[1])



a = GetPrice(".price span")
print(a)

a = data.select(".mg0 a")
short_descs = [sd["href"] for sd in a]
short_descs = "".join(str(x) for x in short_descs)
print(short_descs)
css = "cm-7"
a = sssurl(short_descs, css)
to = a[0]
for x in range(0, len(ReadConfig)):
    Config = ReadConfig[x]
    print(Config['data_element']['description']['data_element'])
    d = to.find(itemprop=Config['data_element']['description']['data_element'])
    print(d)
    d = d.select(Config['data_element']['description']['content'])

print(d)


'''
a = open('WebCrawlerConfig.json')
ReadConfig = json.load(a)
base_url = str(ReadConfig['site']) + "/" + str(ReadConfig['path']) + "/"
print(base_url)
page = urllib.request.urlopen(base_url)
soup = BeautifulSoup(page, 'html.parser')
get_data = soup.find_all(class_=ReadConfig['key_class'])
to = get_data[0]

print(ReadConfig['data_element']['title'])
a = to.select(ReadConfig['data_element']['title'])
short_descs = [sd.get_text() for sd in a]
print(short_descs)
'''

