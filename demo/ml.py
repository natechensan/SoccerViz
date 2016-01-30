__author__ = 'Zhencheng'
#!/usr/bin/python
# -*- coding: utf-8 -*-
import os
import sqlite3 as sqlite
import sys
import csv
import json
import numpy as np
from sklearn import linear_model, datasets
from sklearn.ensemble import RandomForestClassifier 
from sklearn import svm
from sklearn.datasets import fetch_20newsgroups_vectorized
from sklearn.feature_selection import chi2
from sklearn.neighbors import KNeighborsClassifier
from scipy.stats.stats import pearsonr
# from scipy import stats as stats

print "start"
    
def learn(trainX, trainY, testX, model='lr'):
        predictedY = []
        if model=='rf':
        # random forest:
            forest = RandomForestClassifier(n_estimators = 100)
            forest = forest.fit(trainX, trainY)
            predictedY = forest.predict(testX)

        elif model=='lr':
        # logistic regression:
            logreg = linear_model.LogisticRegression(C=1e5)
            logreg.fit(trainX, trainY)
            predictedY = logreg.predict(testX)

        elif model=='svm':
            print 'in svm'
        # svm:        
            svmclf = svm.SVC(decision_function_shape='ovo')
            svmclf.fit(trainX, trainY)
            predictedY = svmclf.predict(testX)
        else:
            print 'in knn'
            # knn = KNeighborsClassifier()
            knn = KNeighborsClassifier(algorithm='auto', leaf_size=30, metric='minkowski', metric_params=None, n_jobs=1, n_neighbors=5, p=2, weights='uniform')
            knn.fit(trainX, trainY) 
            predictedY = knn.predict(testX)
        return predictedY

BASE_DIR = os.path.dirname(os.path.abspath('/Users/zhenchengwang/Desktop/Data Visual Analytics/Sproject'))
db_path = os.path.join(BASE_DIR, 'soccerviz_new')

# final_preditction_last.csv


path = os.path.expanduser('soccerviz')
# print os.getcwd()
con = sqlite.connect('soccerviz_new')
cur = con.cursor()

cur.execute("SELECT distinct(id_league), league_name FROM tournament_info ORDER BY id_league")
leagues = cur.fetchall()

leagues_list = []
line = ""
cur.execute("SELECT region_name, region_code FROM r_name_code ")
mappings = cur.fetchall()
c_n = {}
for mapping in mappings:
    c_n[mapping[1]] = mapping[0]


# for each team now we have a list of countries
for league in leagues:
    print league
    if league[0]==1:
        lookup = 'league_'+str(league[0])
        cur.execute("SELECT rlist FROM "+lookup)
        places_raw = cur.fetchall()
        regsize = len(places_raw)
        dataX = []
        dataY = []
        places = []
        for place in places_raw:
            places.append(place[0].encode('ascii'))
        print places[0]

        cur.execute("SELECT id_tournament, season FROM tournament_info WHERE id_league = ? ORDER BY season", (league[0],))
        seasons = cur.fetchall()

        # get all rosters per season per team => country distribution
        all_distrib = {}
        seasons_list = []
        for season in seasons:
            cur.execute("SELECT id_team, name_team, url_logo FROM team_info WHERE id_team IN (SELECT distinct(id_team) FROM match_info WHERE id_tournament = ?)", (season[0],))
            teams = cur.fetchall()

            teams_list = []
            for team in teams: # for one team, get all its matches_home and matches_away
                # players information per season per team
                cur.execute("""SELECT season_players.id_player, name, position, nationality, ISO3166_1_Alpha_3, region_code, appearances
                                FROM (SELECT distinct(M.id_player), count(M.id_player) AS appearances
                                      FROM match_player_results AS M
                                      WHERE id_team = ? AND id_match IN (SELECT id_match FROM match_info WHERE id_tournament = ? AND (id_team = ? OR id_opponent = ?))
                                GROUP BY M.id_player
                                ORDER BY M.id_player) AS season_players, players_info, country_codes
                                WHERE season_players.id_player = players_info.id_player AND players_info.nationality = country_codes.name_country""", (team[0], season[0], team[0], team[0],))
                season_roster = cur.fetchall()
                
                distrib = {}
                for place in places:    
                    # print place[0].encode('ascii')
                    distrib[place] = 0
                # roster_list = []
                
                for player in season_roster: # get country distribution
                    # if player[3]=='England' and player[3] in ['England','France']:
                    #     print 'correct'
                    #     print player[3]
                    if player[3] in places:  # >= 50
                        # print player[3]
                        distrib[player[3]] = distrib[player[3]] + 1
                    else:                    # < 50, from region code look up region name 
                        region_name = c_n[player[5]]
                        # print region_name
                        distrib[region_name] = distrib[region_name] + 1
                    # line =  "match_importance:"+str(match[8])+"id_player" + str(player[0])+" name" +str(player[1])+ " position" + str(player[2])+" nation_name" +str(player[3])+ " nation_ISO_code" + str(player[4])+ " region_code" + str(player[5])+ " appearances" +str(player[6]);
                    # roster_list.append({"id_player" : player[0], "name" : player[1], "position" : player[2], "nation_name" : player[3], "nation_ISO_code" : player[4], "region_code" : player[5], "appearances" : player[6]})
                hashcode = str(season[0])+","+str(team[0])
                all_distrib[hashcode] = distrib
                # break
                # for key, value in distrib.iteritems():
                #     print key
                #     print value
                # print '*****************************'    


                
        
        print 'out distrib'

        for season in seasons:
            cur.execute("SELECT id_team, name_team, url_logo FROM team_info WHERE id_team IN (SELECT distinct(id_team) FROM match_info WHERE id_tournament = ?)", (season[0],))
            teams = cur.fetchall()
            for team in teams:
                cur.execute("SELECT id_match, A.id_team, id_opponent, B.name_team, goals_scored, goals_against, avg_team_home, avg_team_away, importance FROM match_info AS A, team_info AS B WHERE id_tournament = ? AND A.id_opponent = B.id_team AND A.id_team = ?", (season[0], team[0],))
                matches_home = cur.fetchall()
                # print len(matches_home)

                cur.execute("SELECT id_match, A.id_team, id_opponent, B.name_team, goals_scored, goals_against, avg_team_home, avg_team_away, importance FROM match_info AS A, team_info AS B WHERE id_tournament = ? AND A.id_team = B.id_team AND A.id_opponent = ?", (season[0], team[0],))
                matches_away = cur.fetchall()
                # print len(matches_away)

                matches_list = []
                for match in matches_home:             
                    hashcode1 = str(season[0])+","+str(match[1]) 
                    distrib1 = all_distrib[hashcode1]
                    hashcode2 = str(season[0])+","+str(match[2])
                    distrib2 = all_distrib[hashcode2]
                    myage = match[6]
                    hisage = match[7]
                    imp = match[8]
                    outcome = 0 
                    if match[4]>match[5]:
                        outcome = 1
                    if match[4]<match[5]:
                        outcome = -1
                    currX = []
                    currX.append(imp)
                    currX.append(myage)
                    currX.append(hisage)
                    for value in distrib1.itervalues():
                        currX.append(value)
                    for value in distrib2.itervalues():
                        currX.append(value)

                    # currX.append(match[1])
                    # currX.append(match[2])
                    # currX.append(season[0])

                    currAttrib = np.array(currX)
                    dataX.append(currAttrib)
                    # append the home id and away id
                    
                    dataY.append(outcome)

                    # write to big array
                    # matches_list.append({"id_match" : match[0], "id_team_home" : match[1], "id_team_away" : match[2], "name_team_opponent" : match[3], "goals_home" : match[4], "goals_away" : match[5], "avg_age_team_home" : match[6], "avg_age_team_away" : match[7], "importance" : match[8]})

                for match in matches_away:       
                    hashcode1 = str(season[0])+","+str(match[2])  
                    distrib1 = all_distrib[hashcode1] 
                    hashcode2 = str(season[0])+","+str(match[1])
                    distrib2 = all_distrib[hashcode2] 
                    myage = match[7]
                    hisage = match[6] 
                    imp = match[8]
                    outcome = 0  
                    if match[4]<match[5]:
                        outcome = 1
                    if match[4]>match[5]:
                        outcome = -1    
                    currX = []
                    currX.append(imp)
                    currX.append(myage)
                    currX.append(hisage)
                    for value in distrib1.itervalues():
                        currX.append(value)
                    for value in distrib2.itervalues():
                        currX.append(value)

                    # currX.append(match[1])
                    # currX.append(match[2])
                    # currX.append(season[0])

                    currAttrib = np.array(currX)
                    dataX.append(currAttrib)
                    # append home id and away id
                    
                    dataY.append(outcome)          
                    # matches_list.append({"id_match" : match[0], "id_team_home" : match[1], "id_team_away" : match[2], "name_team_opponent" : match[3], "goals_home" : match[4], "goals_away" : match[5], "avg_age_team_home" : match[6], "avg_age_team_away" : match[7], "importance" : match[8]})
        # with open('season15.csv', 'wb') as csvfile:
        #     writer = csv.writer(csvfile, delimiter=',')
        #     for record in dataX:
        #         if record[-1]==1435:
        #             writer.writerow(record)
        # K = 10
        # accus = []
        # model = 'svm'
        # for fold in range(10):
        #     trainX  = [x for i,x in enumerate(dataX) if i % K != fold]
        #     trainY = [y for i,y in enumerate(dataY) if i % K != fold]
        #     testX =  [x for i,x in enumerate(dataX) if i % K == fold]
        #     testY = [y for i,y in enumerate(dataY) if i % K ==fold]

        #     predictedY = learn(trainX, trainY, testX, model)
        #     results = []
        #     for ind in testY:
        #         results.append(predictedY[ind]==testY[ind])
        #     accu = float(results.count(True))/float(len(results))
        #     accus.append(accu)
        # avg_accu = float(sum(accus))/float(len(accus))
        # print model+':'+str(avg_accu)



        # league level
        dataX = np.array(dataX)
        segX = np.split(dataX, 4)
        trainX = np.concatenate((segX[0],segX[2],segX[3]), axis=0)
        testX = segX[1]

        dataY = np.array(dataY)
        segY = np.split(dataY, 4)
        trainY = np.concatenate((segY[0],segY[2],segY[3]), axis=0)
        testY = segY[1]
        print dataX.shape
        print dataY.shape

        # random forest:
        # forest = RandomForestClassifier(n_estimators = 100)
        # forest = forest.fit(trainX, trainY)
        # predictedY = forest.predict(testX)


        # logistic regression:
        logreg = linear_model.LogisticRegression(C=1e5)
        logreg.fit(trainX, trainY)
        predictedY = logreg.predict(testX)

        # svm:
        # svmclf = svm.SVC(decision_function_shape='ovo')
        # svmclf.fit(trainX, trainY)
        # predictedY = svmclf.predict(testX)

        with open('correlation.csv', 'wb') as csvfile:
            writer = csv.writer(csvfile, delimiter=',')
            for i in range(49):
                attrib = dataX[:,i]  
                writer.writerow(pearsonr(attrib, dataY) )          
                # print str(i)
                # print pearsonr(attrib, dataY) 

            # for record in dataX:
            #     if record[-1]==1435:
            #         writer.writerow(record)
                   
            
            


        accu = 0
        for i in range(len(testY)):
            print testY[i]
            print predictedY[i]
            # print "*********"
            if testY[i]==predictedY[i]:
                # print "correct"
                accu = accu+1
            # print "$$$$$$$$$$"
        accu = float(accu)/float(len(testY))
        print accu        
        # f = open("final_preditction_last.csv")
        # testX = []
        # for row in csv.reader(f):
        #     currX = []
        #     for elem in row:
        #         currX.append(float(elem))
        #     testX.append(currX)
        # Ys = learn(dataX, dataY, testX, model='svm')
        # print 'Ys:'
        # print Ys
        # print "Done with league: ", league[0]
    else:
        break

# print predictedY[0]
# print predictedY[1]


print line
leagues_dict = {"leagues" : leagues_list}

# file_object = open("english.json", "w")
# file_object.write(json.dumps(leagues_dict, indent=3, separators=(',', ':')))
# file_object.close()

print "end"
