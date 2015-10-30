import urllib2
import json
import re
import time
import csv

from urllib2 import Request, urlopen


csvfile = open ('english_teams_clean.csv','r')
reader = csv.reader(csvfile)
team_names=[]
for row in reader:
    team_names.append(row[0])
print (team_names)

#format date
def search(api, team, page, date_from, date_to): #function to search movies with keyword
    headers = {
    'Accept': 'application/json'
    }
    url=('http://content.guardianapis.com/search?show-fields=body&from-date='+date_from+'&to-date='+date_to+'&page='+str(page)+'&' \
         'section=football&q='+team+'&api-key='+api+'&order-by=relevance&tag=type%2Farticle')
    request=urllib2.Request(url)
    response = urlopen(request).read()
    data = json.loads(response)
    return data

def create_file(name): #creates files
    file = open(name,"wb")
    return file

def write_file(file, info): #write iles
    file.write(bytes(info + "\n"))


year_start=2015
num_files=4

for y in range(0,num_files):
    year=year_start-y

    jsonfile = open ('news_'+ str(year) +'.json','w')

    api_key='5uxf9msm3mzdahyrybnq3cd6'
    #api_key='test'
    name='urls.txt'
    team='arsenal'
    team_array=team_names
    #team_array=['Accrington Stanley','AFC Wimbledon','Manchester United','Manchester City', 'watford','manchester united']
    number=200

    date_from=str(year)+'-08-01'
    date_to=str(year+1)+'-05-31'
    file = create_file(name)

    data_output={"year":date_from, "response":[]}

    for team in team_array:
        team = team.lower()
        if (' ' in team):
            team_aux=team.replace (" ", "%20")
        else:
            team_aux=team
        print(team_aux)

        aux_title=0
        if (len(team.split())>1):
            if ((str(team).find('united') + str(team).find('city'))>1):
                team_flag=1
            else:
                team_flag=0
        else:
                team_flag=1
        data_response={"name":team,"results":[]}
        flag_out=0
        counter=0
        page=1
        article_id=0
        data = search(api_key, team_aux, page, date_from, date_to)
        total_pages= data['response']['pages']
        #while counter<number:
        while (counter<number and page<=total_pages and flag_out<80):
          data = search(api_key, team_aux, page, date_from, date_to)
          for item in data['response']['results']:
            title=item['webTitle']
            url=item['webUrl']
            if 'fields' in item:
                if 'body' in item['fields']:
                    body=item['fields']['body']
                    body = re.sub('<[^>]*>', '', body) #to remove tags
                    body=body.encode('utf8')
                    output = (team + ", " + url)
                    title=title.encode('utf8')
                    #print(title)

                    #count team name words
                    aux_title=0
                    if (team_flag==1):
                        aux_title=(str(title).lower().find(team))
                    else:
                        for x in range(0,len(team.split())):
                            aux_title+=(str(title).lower().find(team.split()[x])) #find word in title

                    if(aux_title > -1):
                        #print (body)
                        article_id+=1
                        write_file(file, output)
                        counter+=1
                        data_results={"article_id":article_id, "title":title, "url":url, "body":body}
                        data_response['results'].append(data_results)
                        #print counter
                    else:
                        flag_out+=1
                    if (counter>=number):
                        break
          page+=1
        print counter
        data_output['response'].append(data_response)
    file.close()

    json_str=json.dumps(data_output, indent=4)
    jsonfile.write(json_str)
    jsonfile.close()
