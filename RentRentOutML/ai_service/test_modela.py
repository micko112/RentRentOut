import torch
import torch.nn as nn
import joblib
import warnings
import csv
import io
warnings.filterwarnings("ignore") 

csv_data = """id,name,parent_id
100,Građevinska oprema i alati,
101,Bušilice i šrafilice,100
102,Struja i agregati,100
103,Testerisanje i sečenje,100
104,Brušenje i poliranje,100
105,Ventilacija i grejanje,100
106,Merni instrumenti,100
107,Ručni alati,100
108,Krečenje i tapete,100
109,Glodanje i hoblovanje,100
110,Skele i podupirači,100
200,Elektronika,
201,Audio oprema i zvuk,200
202,Dronovi,200
203,Projektori i televizori,200
204,Računari i oprema,200
205,Video igre i konzole,200
206,Mobilni telefoni i tableti,200
207,Radio stanice i toki-voki,200
208,Kancelarijske mašine,200
209,POS terminali,200
210,Ostalo u elektronici,200
300,Foto i Video,
301,Fotoaparati i kamere,300
302,Objektivi,300
303,Blicevi i rasveta,300
304,Stativi i rigovi,300
305,Foto/Video paketi,300
306,Monitori,300
307,Baterije za kamere,300
308,Memorijske kartice,300
309,Foto pozadine,300
310,Ostalo u foto i video,300
400,Bašta,
401,Baštenske mašine,400
402,Merdevine,400
403,Održavanje travnjaka,400
404,Baštenski alati,400
405,Baštenski nameštaj,400
406,Ostalo u bašti,400
500,Kuća i dom,
501,Stilizovanje i dekoracija,500
502,Čišćenje i pranje,500
503,Kuhinjski aparati,500
504,Oprema za decu,500
505,Lična nega,500
506,Mašine za šivenje,500
507,Oprema za selidbe,500
508,Oprema za kućne ljubimce,500
509,Ostalo u kući,500
600,Proslave i događaji,
601,Zvuk rasveta i bina,600
602,Odeća i kostimi,600
603,Nameštaj za proslave,600
604,Ketering i kuhinja,600
605,Aktivnosti i zabava,600
606,Šatori i tende,600
607,Dekoracija,600
608,Party paketi,600
609,Grejalice i pečurke,600
610,Ostalo za proslave,600
700,Sport i rekreacija,
701,Muzički instrumenti,700
702,Kampovanje i outdoor,700
703,Biciklizam,700
704,Igra i hobi,700
705,Sportski rekviziti,700
706,Sportovi na vodi,700
707,Trening i teretana,700
708,Zimski sportovi,700
709,Putovanja i koferi,700
710,Mobilnost i medicinska nega,700
800,Vozila i oprema,
801,Auto oprema,800
802,Alati za radionicu,800
803,Čamci i plovila,800
804,Prikolice,800
805,Kamperi i kamp prikolice,800
806,Motocikli i oprema,800
807,Prikolice za konje,800
808,Ostala vozila,800
900,Ostalo,
901,Prostori,900
1001,Komprimovani vazduh,100
1002,Građevinska mehanizacija,100
1003,Podne obloge i pločice,100
1005,Mašine za uređenje terena,100
1006,Klamerice i ekseri,100
1007,Vodoinstalaterski alati,100
1008,Zaštitna oprema,100
1009,Transport i dizanje,100
1010,Paketi alata,100
1011,Aparati za zavarivanje,100
1012,Radna rasveta,100
1013,Ostalo u građevini i alatima,100
1015,Inverter,200
1017,Satelitska oprema,200
1018,Pametni zvučnik,200
1019,Baterije i punjači,102
1020,Skidač izolacije,107
1021,Kalemovi i električni kablovi,107
1022,Razvodni ormar,102
1024,Agregat,102
1025,Aligator testera,103
1026,Tračna testera,103
1027,Sekač vijaka,103
1028,Cirkularna testera,103
1029,Kombinovana testera,103
1030,Vodilica,103
1031,Ručna testera,103
1032,Ubodna testera,103
1033,Makaze za metal,103
1034,Kružna testera za metal,103
1035,Stoni ger za drvo,103
1036,Ručni ger za drvo,103
1037,Mobilna pilana,103
1038,Oscilirajući višenamenski alat / Multialat,103
1039,Plazma rezač,103
1040,Narezač,103
1041,Presa,103
1042,Sabljasta testera,103
1043,Stona testera,103
1044,Rezač pločica,103
1045,Ostalo u testerama,103
1046,Ugaona brusilica,104
1047,Trakasta turpija,104
1048,Trakasta brusilica,104
1049,Stona brusilica,104
1050,Precizna brusilica,104
1051,Brusilica za suvi zid,104
1052,Mašina za graviranje,104
1053,Brusilica za podove,104
1054,Toplotni pištolj,104
1055,Orbitalna brusilica,104
1056,Polirka,104
1057,Listna brusilica,104
1058,Ostalo u brušenju,104
1059,Ostale brusilice,104
1060,Merač protoka vazduha,106
1061,Merač kvaliteta vazduha,106
1062,Uglomer,106
1063,Tražač kabla,106
1064,Merač energije,106
1065,IC termometar,106
1066,Laserski daljinomer,106
1067,Laserski nivelator,106
1068,Fotometar,106
1069,Metar,106
1070,Detektor metala,106
1071,Merač vlage,106
1072,Multimetar,106
1073,Osciloskop,106
1074,Detektor radona,106
1075,Rotirajući laser,106
1076,Vaga,106
1077,Merač buke,106
1078,Libela,106
1079,Tražač greda,106
1080,Termovizijska kamera,106
1081,Tahimetar,106
1082,Ostalo u mernim instrumentima,106
1083,Pištolj za silikon,107
1084,Stega i mengel,107
1085,Pijuk,107
1086,Regulator šarki,107
1087,Pištolj za lepak,107
1088,Čekić,107
1089,Udarni odvijač,107
1090,Klješta,107
1091,Izvlakač,107
1092,Macola,107
1093,Lemilica,107
1094,Ključevi i gedore,107
1095,Kofer za alat,107
1096,Moment ključ,107
1098,Ključ,107
1099,Ostalo u ručnim alatima,107
1100,Mešalica boje,108
1101,NCS karta boja,108
1102,Merač boja,108
1103,Skidač boje,108
1104,Nogari za farbanje,108
1105,Pištolj za boju,108
1106,Sto za tapete,108
1107,Prskalica za malter,108
1108,Kutija za valjak,108
1109,Skidač tapeta,108
1110,Ostalo u farbanju,108
1111,Ručno rende,109
1112,Strug,109
1113,Glodalica,109
1114,Stol za glodalicu,109
1115,Stono rende,109
1116,Ostalo u glodanju,109
1117,Viljuškar,1009
1118,Vakuumski podizač stakla,1009
1119,Paletni viljuškar,1009
1120,Platforma za prevoz,1009
1121,Kolica za teret,1009
1122,Vitlo,1009
1123,Ostalo u transportu,1009
1124,DJ oprema,201
1125,Slušalice,201
1126,Luper,201
1127,Mikrofon,201
1128,Muzički instrumenti,201
1129,PA sistem,201
1130,Prenosni zvučnik,201
1131,Zvučna kartica,201
1132,Zvučnik,201
1133,Ostalo u zvuku,201
1134,Oprema za dronove,202
1135,DJI Avata,202
1136,DJI FPV,202
1137,DJI Inspire,202
1138,DJI Mavic,202
1139,DJI Mavic 3,202
1140,DJI Phantom,202
1141,DJI Spark,202
1142,Podvodni dronovi,202
1143,Ostali dronovi,202
1144,4G ruter,204
1145,Računarska oprema,204
1146,Desktop računar,204
1147,Grafička tabla,204
1148,Laptop,204
1149,Mrežna oprema,204
1150,PC monitori,204
1151,Ostalo u računarima,204
1152,Arkadna mašina,205
1153,Igrica,205
1154,Konzola,205
1155,Ručni kontroler,205
1156,Simulator oprema,205
1157,VR naočale,205
1158,Ostalo u video igrama,205
1159,Oprema za mobilne i tablete,206
1160,Gimbal za mobilni,206
1161,Mobilni telefon,206
1162,Pametni sat,206
1163,Solarni punjač,206
1164,Tablet,206
1165,3D štampač,208
1166,Štampač nalepnica,208
1167,Mašina za laminiranje,208
1168,Laserski rezač,208
1169,Rezač papira,208
1170,Skener za fotografije,208
1171,Štampač,208
1172,Skener,208
1173,Rezač dokumenata,208
1174,Ostalo u kancelarijskim mašinama,208
1175,Ugaona bušilica,101
1176,Set bitova,101
1177,Pikamer,101
1178,Burgije,101
1179,Mašina za bušenje,101
1180,Krunska testera,101
1181,Udarna bušilica,101
1182,Udarni ključ,101
1183,Stub bušilica,101
1184,Ankeri i tiplovi,101
1185,Rotacioni čekić,101
1186,Šrafilice,101
1187,Mašina za bušenje kamena,101
1188,Ostalo u bušilicama i šrafilicama,101
1189,Vazdušni pištolj,1001
1190,Crevo za komprimovani vazduh,1001
1191,Kompresor,1001
1192,Ostalo u komprimovanom vazduhu,1001
1193,Platforma za rad na visini,1002
1194,Damper,1002
1195,Bager,1002
1196,Utovarivač,1002
1197,Mini damper,1002
1198,Traktor,1002
1199,Ostalo u građevinskoj mehanizaciji,1002
1200,Alat za decking,1003
1201,Brusilica za podove,1003
1202,Mašina za skidanje podova,1003
1203,Paket za podove,1003
1204,Rezač laminata,1003
1205,Rezač pločica,1003
1206,Paket za keramiku,1003
1207,Ostalo u podnim oblogama,1003
1208,Vibro nabijač,1005
1209,Beton i armatura,1005
1210,Građevinska grabulja,1005
1211,Svrdlo za zemlju,1005
1212,Rezač kamena,1005
1213,Ostalo u mehanizaciji terena,1005
1214,Pištolj za eksere,1006
1215,Pištolj za klamerice,1006
1216,Alati za proširivanje,1007
1217,Rezač cevi,1007
1218,Ključ za cevi,1007
1219,Savijač cevi,1007
1220,Presa,1007
1221,Tester pritiska,1007
1222,Sanipex alati,1007
1223,Ostalo u vodoinstalaciji,1007
1224,Električni aparat za zavarivanje,1011
1225,Gasni aparat za zavarivanje,1011
1226,MIG aparat,1011
1227,Plastični zavarivač,1011
1228,Maska za zavarivanje,1011
1229,Ostalo u zavarivanju,1011
1230,Kolica za građevinski otpad,110
1231,Gradilišna ograda,110
1232,Građevinska skela,110
1233,Prekrivači prozora,110
1234,Lift za suvi zid,110
1235,Podupirač,110
1236,Nogari za farbanje,110
1237,Sto za tapete,110
1238,Kolica za gipsane ploče,110
1239,Radna platforma,110
1240,Radni sto,110
1241,Ostalo u skelama,110
1242,Veliki ekran,203
1243,DVD i Blu-ray,203
1244,Media plejeri,203
1245,Grafoskop,203
1246,Projektor,203
1247,Projekciono platno,203
1248,Slajd projektor,203
1249,TV,203
1250,VHS plejer,203
1251,Ostalo u projektorima i TV,203
1252,Kamkorder,301
1253,Kompaktni fotoaparat,301
1254,DSLR fotoaparat,301
1255,Digitalna kino kamera,301
1256,Filmska kamera,301
1257,Mirrorless kamera,301
1258,Polaroid kamera,301
1259,Specijalne kamere,301
1260,Ostale kamere,301
1261,Torba za kameru,301
1262,Senzor boja,301
1263,Follow focus i nosač objektiva,301
1264,Foto štampač,301
1265,Streaming oprema,301
1266,Teleprompter,301
1267,Tela kamera za ronjenje,301
1268,Vikendica,901
1269,Kancelarija,901
1270,Parking i garaža,901
1271,Skladišna jedinica,901
1272,Studio,901
1273,Ostali prostori,901
1274,Kinematografski prime objektivi,302
1275,Konverter,302
1276,DSLR objektivi,302
1277,Filter za objektiv,302
1278,Objektivi za srednji format,302
1279,Mirrorless objektivi,302
1280,Oprema za objektive,302
1281,Ostali objektivi,302
1282,Blic,303
1283,Okidač blica,303
1284,Reflektorski ekran,303
1285,Ring svetlo,303
1286,Softbox i kišobran,303
1287,Speed Booster,303
1288,Studijski blic,303
1289,Studijska lampa,303
1290,Ostalo u blicevima i rasveti,303
1291,C-stalak,304
1292,Kamera na kablu,304
1293,Rigovi i ramena za kameru,304
1294,Nosač za automobil,304
1295,Stege i hvatači,304
1296,Kran i jib,304
1297,Gimbali i stabilizatori,304
1298,Gorillapod,304
1299,Monopod,304
1300,Slajder i dolly,304
1301,Tronožac,304
1302,Glava tronošca,304
1303,Ostalo u stativima,304
1304,Zeleni ekran,309
1305,Jednobojna pozadina,309
1306,Pozadina sa motivom,309
1307,Pozadina sa uzorkom,309
1308,Ostalo u pozadinama,309
1309,Motorna testera,401
1310,Trimer za travu,401
1311,Pumpa za odvodnjavanje,401
1312,Mašina za drva,401
1313,Baštensko sito,401
1314,Baštenski drobilica,401
1315,Alati za čišćenje oluka,401
1316,Makaze za živicu,401
1317,Duvač lišća,401
1318,Cepač drva,401
1319,Usisivač za lišće,401
1320,Čistač terasa,401
1321,Robot za bazen,401
1322,Visokotlačni perač,401
1323,Čistač krova,401
1324,Freza,401
1325,Freza za panjeve,401
1326,Mašina za metenje,401
1327,Drobionica drveta,401
1328,Ostalo u baštenskim mašinama,401
1329,Kombinovane merdevine,402
1330,Obične merdevine,402
1331,Merdevine za stepenište,402
1332,Stolica-stepenik,402
1333,Stepenik,402
1334,Teleskopske merdevine,402
1335,Detektor kablova za robotske kosilice,403
1336,Mašina za polaganje kablova,403
1337,Kompostni valjak,403
1338,Baštenski valjak,403
1339,Trimer za ivice travnjaka,403
1340,Grabulje za nivelisanje,403
1341,Kosilica,403
1342,Ostalo u održavanju travnjaka,403
1343,Skarifajzer,403
1344,Rasipač za đubrivo,403
1345,Sekira,404
1346,Bure za spaljivanje,404
1347,Berač voća,404
1348,Baštensko crevo,404
1349,Leđna prskalica,404
1350,Aerator sa šupljim klinovima,404
1351,Gvozdena šipka,404
1352,Zaštita od štetočina,404
1353,Kramp,404
1354,Pobijač stubova,404
1355,Vrtlarska testera i škare,404
1356,Kolica za baštu,404
1357,Lopata,404
1358,Lopata za sneg,404
1359,Nabijač,404
1360,Kontrola korova,404
1361,Kolica za prevoz,404
1362,Ostalo u baštenskim alatima,404
1363,Pomoćni krevet,501
1364,Nameštaj,501
1365,Slike,501
1366,Rekviziti,501
1367,Stilizovanje,501
1368,Ostalo u dekoraciji,501
1369,Čistač tepiha i tapaciringa,502
1370,Parna presa za odeću,502
1371,Čistač odvoda,502
1372,Pegla,502
1373,Ribač podova,502
1374,Parna mašina za čišćenje,502
1375,Ultrazvučno čišćenje,502
1376,Usisivač,502
1377,Čistač prozora,502
1378,Ostalo u čišćenju i pranju,502
1379,Varenje piva i vinarstvo,503
1380,Dehidrator hrane,503
1381,Centrifuga za med,503
1382,Oštrač noževa,503
1383,Mini rerna,503
1384,Lonci i tiganje,503
1385,Sous Vide aparat,503
1386,Sokovnik na paru,503
1387,Kuhinjski pribor i aparati,503
1388,Ostalo u kuhinji,503
1389,Nosiljka i stolica za bebu,504
1390,Bebi monitor,504
1391,Grejač bočica,504
1392,Pumpa za majčino mleko,504
1393,Autosedište,504
1394,Dečiji bicikli i oprema,504
1395,Dečija odeća,504
1396,Dečiji nameštaj,504
1397,Jastuk za dojenje,504
1398,Prostirka za igru,504
1399,Kolica i oprema,504
1400,Igračke,504
1401,Putovanje s decom,504
1402,Ostalo za decu,504
1403,Merač krvnog pritiska,505
1404,Epilacija,505
1405,Stilizovanje kose,505
1406,Ostalo u ličnoj nezi,505
1407,Kutije za selidbu,507
1408,Kolica za selidbu,507
1409,Remeni i pojasevi za selidbu,507
1410,Kolica za teret,507
1411,Ostalo u selidbi,507
1412,Mašinica za šišanje pasa,508
1413,Inkubator za jaja,508
1414,Torba za kućne ljubimce,508
1415,Ostalo za kućne ljubimce,508
1416,Mašina za balone,601
1417,Party rasveta,601
1418,Mašina za dim,601
1419,Zvuk,601
1420,Bina,601
1421,Ostalo u rasveti i dimu,601
1422,Dečija odeća,602
1423,Čuvanje odeće,602
1424,Kostimi i maskiranje,602
1425,Party odeća,602
1426,Ostalo u odeći,602
1427,Šank,603
1428,Bar sto,603
1429,Stolice za proslave,603
1430,Paket stolovi i stolice,603
1431,Sklopivi stolovi,603
1432,Ostalo u nameštaju za proslave,603
1433,Roštilj,604
1434,Chafing dish,604
1435,Rešo,604
1436,Pića,604
1437,Paella tava,604
1438,Pizza peć,604
1439,Kolica za kobasice,604
1440,Lonac za supu,604
1441,Posuđe i pribor za jelo,604
1442,Dispenser za šlag,604
1443,Ostalo u ketering kuhinji,604
1444,Beer pong,605
1445,Skakavac,605
1446,Bazen sa mehurićima,605
1447,Fontana od čokolade,605
1448,Mašina za šećernu vunu,605
1449,Nargila,605
1450,Jakuzi,605
1451,Sladoled i slush mašine,605
1452,Karaoke,605
1453,Photo booth,605
1454,Kokičar,605
1455,Sauna,605
1456,Oprema za voditelja,605
1457,Ostalo u aktivnostima,605
1458,Šator 0-23 m²,606
1459,Šator 24-32 m²,606
1460,Šator preko 32 m²,606
1461,Ostalo u šatorima,606
1462,Pumpica za balone,607
1463,Sveća i svećnjak,607
1464,Diskokugla,607
1465,Košara za vatru,607
1466,Crveni tepih,607
1467,Stilizovanje i dekoracija,607
1468,Ostalo u dekoraciji,607
1469,Pojačivač,701
1470,Bas gitara,701
1471,Čelo,701
1472,Drum mašina i sampler,701
1473,Bubnjevi,701
1474,Gitara,701
1475,Gitarni pedal,701
1476,Instrumentalni mikrofon,701
1477,Klavijatura,701
1478,Klavir,701
1479,Zvučna kartica,701
1480,Stalak i kabl,701
1481,Sintisajzer,701
1482,Ukulele,701
1483,Violina,701
1484,Duvački instrument,701
1485,Ostali muzički instrumenti,701
1486,Ruksak,702
1487,Berba šumskih plodova,702
1488,Dvogled,702
1489,Kampovanje,702
1490,Kolica za outdoor,702
1491,Nosiljka za dete,702
1492,Rashladna kutija i torba,702
1493,Vodootporne torbe,702
1494,Čeona lampa,702
1495,Planinski štap,702
1496,Planinarsko oruđe,702
1497,Lov i ribolov,702
1498,Zaštita od komaraca,702
1499,Navigacija i komunikacija,702
1500,Outdoor odeća,702
1501,Outdoor paket,702
1502,Ostalo u outdoor-u,702
1503,Prikolica za teret na biciklu,703
1504,Nosač bicikla,703
1505,Prikolica za dete (bicikl),703
1506,Torba za transport bicikla,703
1507,Bicikli,703
1508,Dečiji bicikl,703
1509,Ostalo u biciklizmu,703
1510,Air track,704
1511,Airsoft,704
1512,Društvene igre i slagalice,704
1513,Bumper ball,704
1514,Štafelaj,704
1515,Fly jumpers,704
1516,Stoni fudbal,704
1517,Termopresa,704
1518,Vozila za hobi,704
1519,Longboard i skejtbord,704
1520,Ribolov magnetom,704
1521,Mikroskop,704
1522,Igre na otvorenom,704
1523,Paintball,704
1524,Poker set,704
1525,Stoni hokej,704
1526,Mašina za tetoviranje,704
1527,Trampolin,704
1528,Vodene igre,704
1529,Ostalo u igranju i hobiju,704
1530,Odbojka na pesku,705
1531,Penjanje,705
1532,Jahanje,705
1533,Floorball,705
1534,Fudbal,705
1535,Golf,705
1536,Roleri,705
1537,Reket sportovi,705
1538,Slackline,705
1539,Ostalo u sportu,705
1540,Čamac na gumu,706
1541,Ronilačka oprema,706
1542,Električni surfboard,706
1543,Jetski,706
1544,Kajak,706
1545,Kiteboarding,706
1546,Paddleboard,706
1547,Surfanje,706
1548,Vodni ski i wakeboard,706
1549,Odeća za vodene sportove,706
1550,Windsurfing,706
1551,Wing surf,706
1552,Ostalo u vodenim sportovima,706
1553,Crosstrainer,707
1554,"Bučice, tegovi i kettlebells",707
1555,Sobni bicikl,707
1556,Sprave za vežbanje,707
1557,Pojas za hidrataciju,707
1558,Masaža,707
1559,Šipka za zgibove,707
1560,Prsluk sa tegovima,707
1561,Yoga prostirka,707
1562,Ostalo u vežbanju i teretani,707
1563,Oprema za lavine,708
1564,Ekspedicione sanke,708
1565,Hokej,708
1566,Skijaški roleri,708
1567,Klizaljke,708
1568,Skijanje i snowboard,708
1569,Sanke,708
1570,Penjanje po snegu i ledu,708
1571,Snowracer,708
1572,Zimska odeća,708
1573,Ostalo u zimskim sportovima,708
1574,Kofer za bicikl,709
1575,Nosiljka za kućnog ljubimca,709
1576,Specijalni kofer,709
1577,Koferi,709
1578,Putni adapter,709
1579,Putovanje s decom,709
1580,Ostalo u putovanjima,709
1581,Štake,710
1582,Električna invalidska kolica,710
1583,Prva pomoć i CPR,710
1584,Hodalica,710
1585,Invalidska kolica,710
1586,Ostalo u mobilnosti,710
1587,Punjač baterija za vozila,801
1588,Nosač bicikla na automobilu,801
1589,Nosač za kanu i kajak,801
1590,Grejač kabine,801
1591,Autosedište,801
1592,Punjač za električni auto,801
1593,Jump starter,801
1594,Nosač tereta za kuku,801
1595,Krovni kofer,801
1596,Krovni nosač za ski,801
1597,Krovni nosač,801
1598,Lanci za sneg,801
1599,Ostalo u auto opremi,801
1600,Kolica za montažu,802
1601,Punjač baterija za vozila,802
1602,Auto alati,802
1603,Dijagnostički alat,802
1604,Dizalice,802
1605,Indukcioni grejač,802
1606,Dizalica i stalci,802
1607,Jump starter,802
1608,Alat za lanac motocikla,802
1609,Dizalica za motocikl,802
1610,Menjanje ulja,802
1611,Rampa,802
1612,Vakuum tester,802
1613,Alati za točkove i gume,802
1614,Radioničarska dizalica,802
1615,Radioničarska presa,802
1616,Ostalo u radionici,802
1617,Oprema za čamce,803
1618,Prikolica za čamac,803
1619,Kanu,803
1620,Jetski,803
1621,Motorni čamac,803
1622,Čamac na vesla i dinghy,803
1623,Jedrilica,803
1624,Jedriličarska odeća,803
1625,Prikolica za čamac,804
1626,Prikolica sa kavezom,804
1627,Transporter za automobile,804
1628,Zatvorena prikolica,804
1629,Prikolica za motocikl,804
1630,Otvorena prikolica,804
1631,Prikolica za drvo,804
1632,Tow dolly,804
1633,Prikolica s pokrivačem,804
1634,Ostalo u prikolicama,804
1635,Kamp prikolica,805
1636,Ostalo u kamperima,805
1637,Motociklistička oprema,806
1638,Prikolica za motocikl,806
1639,Ostalo u motociklima,806
1640,Prikolica za konje,807
"""

id_u_ime = {}
reader = csv.DictReader(io.StringIO(csv_data.strip()))
for row in reader:
    id_u_ime[int(row['id'])] = row['name']
    
class RentRentOutKategorizator(nn.Module):
    def __init__(self, ulazna_velicina, broj_klasa):
        super(RentRentOutKategorizator, self).__init__()
        self.mreza = nn.Sequential(
            nn.Linear(ulazna_velicina, 256),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(256, 128),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(128, broj_klasa)
        )

    def forward(self, x):
        return self.mreza(x)

print("Učitavam mozak mreže...")
# 2. UČITAVANJE ARTIFAKATA
vectorizer = joblib.load("tfidf_vectorizer.pkl")
label_encoder = joblib.load("label_encoder.pkl")

ulazna_velicina = len(vectorizer.vocabulary_)
broj_klasa = len(label_encoder.classes_)

model = RentRentOutKategorizator(ulazna_velicina, broj_klasa)
model.load_state_dict(torch.load("rentrentout_model.pth", map_location=torch.device('cpu'), weights_only=True))
model.eval() # OBAVEZNO: Gasimo učenje!
print("Spreman za testiranje!\n" + "="*40)

# 3. INTERAKTIVNA PETLJA ZA TESTIRANJE
while True:
    tekst = input("\nUnesi naslov oglasa (ili ukucaj 'x' za izlaz): ")
    
    if tekst.lower() == 'x':
        print("Kraj testiranja. Pozdrav!")
        break
        
    if len(tekst.strip()) < 3:
        print("Unesi malo duži tekst!")
        continue

    # a) Pretvaranje teksta u tenzore
    X_novi = vectorizer.transform([tekst]).toarray()
    X_tenzor = torch.tensor(X_novi, dtype=torch.float32)
    
    with torch.no_grad():
        izlaz = model(X_tenzor) # Sirovi brojevi (logits)
        
        # b) MAGIJA: Pretvaramo sirove brojeve u procente (od 0.0 do 1.0)
        procenti = torch.nn.functional.softmax(izlaz, dim=1)
        
        # c) TOP 5: Tražimo 5 najvećih procenata i njihove indekse
        top5_procenti, top5_indeksi = torch.topk(procenti, 5)

    print("\n AI razmišlja... Ovo su top 5 kategorija:")
    print("-" * 40)
    
    # topk nam vraća tenzore unutar tenzora, pa ih "razmotavamo" sa [0]
    for i in range(5):
        verovatnoca = top5_procenti[0][i].item() * 100  # Množimo sa 100 da dobijemo recimo 85.5%
        indeks = top5_indeksi[0][i].item()
        
        # Prevodimo onaj 0-299 indeks nazad u tvoj ID iz baze (npr. 1341)
        pravi_id = label_encoder.inverse_transform([indeks])[0]
        
        ime_kategorije = id_u_ime.get(pravi_id, "Nepoznata kategorija")

    
        print(f"{i+1}. {ime_kategorije:<35} (ID: {pravi_id:4d}) | Sigurnost: {verovatnoca:5.2f}%")