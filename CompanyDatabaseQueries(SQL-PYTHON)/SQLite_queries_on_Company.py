# This program ultilizes the SQLite RDMS with a database called Company that includes the tables
# emp, mgr, parts, projects, spj, AND suppliers.

import sqlite3
import sys
from collections import namedtuple

fileName = sys.argv[1]

def printQuery(dml):
	for q in dml:
		print q
	for q in dml:
		cur.execute(q)
		row = cur.fetchone()
		if row is not None:
			x = ""
			for e in row:
				x += "{:>12}"
			while row != None:
				print(x.format(*row))
				row = cur.fetchone() 

dict = {1: ["SELECT * FROM parts WHERE pcity = 'London' AND color = 'Red'"], 
2: ["SELECT * FROM parts WHERE pcity = 'London' AND color = 'Red'"],
3: ["SELECT pname, qty FROM (SELECT * FROM parts,spj WHERE parts.pno = spj.pno) WHERE color='Red' AND pcity = 'London' ORDER BY pname ASC, qty ASC"],
4: ["SELECT pcity, pname, weight, color FROM parts ORDER BY pcity ASC, pname ASC"],
5: ["SELECT * FROM parts, projects WHERE parts.pcity = projects.jcity ORDER BY pno ASC, weight ASC, pcity ASC, jno ASC"],
6: ["SELECT * FROM parts,projects WHERE parts.pcity = projects.jcity AND parts.color = 'Red' AND parts.pname = 'Screw' ORDER BY pno ASC, jno ASC"],
7: ["SELECT * FROM suppliers, (SELECT * FROM projects,emp) WHERE scity = jcity AND jcity = city AND city = scity ORDER BY sno ASC, jno ASC"],
8: ["SELECT sno, first, last FROM suppliers,emp WHERE sname = last ORDER BY sno ASC"], 
9: ["SELECT * FROM parts WHERE pcity = 'London' OR pcity = 'Paris' OR pcity = 'New YORk' ORDER BY pno ASC"],
10: ["SELECT jno, jname, jcity FROM (SELECT * FROM projects, (SELECT * FROM (SELECT last AS mlast, first AS mfirst, dept FROM mgr), emp WHERE last = mlast AND first = mfirst) WHERE city = jcity) ORDER BY jno ASC"],
11: ["SELECT pname, sname, jname, qty FROM (SELECT * FROM spj,(SELECT * FROM parts, (SELECT * FROM suppliers, projects)))"], 

12: ["CREATE TEMP TABLE ATEMP AS SELECT parts.pname FROM spj inner JOIN parts ON parts.pno=spj.pno", \
"CREATE TEMP TABLE BTEMP AS SELECT suppliers.sname FROM spj inner JOIN suppliers ON suppliers.sno=spj.sno", \
"CREATE TEMP TABLE CTEMP AS SELECT projects.jname FROM spj inner JOIN projects ON projects.jno=spj.jno", \
"CREATE TEMP TABLE DTEMP AS SELECT qty FROM spj", \
"SELECT ATEMP.pname FROM ATEMP"],

13: ["SELECT pno, sum(qty) FROM spj WHERE NOT(pno='p1' OR pno='p3') group by pno having not(sum(qty) = 1300)"],
14: ["SELECT * FROM parts LEFT OUTER JOIN spj WHERE parts.pno = spj.pno"],
15: ["SELECT * FROM parts ORDER BY pno ASC"],

16: ["CREATE TEMP TABLE TEMPA AS SELECT parts.pcity FROM parts, suppliers WHERE parts.pcity = suppliers.scity", \
"CREATE TEMP TABLE TEMPB AS SELECT projects.jcity FROM projects,parts WHERE projects.jcity = parts.pcity", \
"CREATE TEMP TABLE TEMPC AS SELECT suppliers.scity FROM projects,suppliers WHERE projects.jcity = suppliers.scity", \
"CREATE TEMP TABLE TEMPD AS SELECT pcity FROM parts UNION SELECT scity FROM suppliers UNION SELECT jcity FROM projects", \
"CREATE TEMP TABLE TEMPE AS SELECT distinct * FROM TEMPA union SELECT * FROM TEMPB union SELECT * FROM TEMPC", \
"CREATE TEMP TABLE TEMPF AS SELECT pcity FROM TEMPD WHERE pcity NOT IN (SELECT pcity FROM TEMPE) ORDER BY pcity ASC", \
"SELECT * FROM TEMPF"],

17: ["CREATE TEMP TABLE TEMPG AS SELECT pcity FROM parts INTERSECT SELECT scity FROM suppliers INTERSECT SELECT jcity FROM projects", \
"SELECT * FROM (SELECT pcity FROM parts UNION SELECT scity FROM suppliers UNION SELECT jcity FROM projects)WHERE pcity NOT IN (SELECT pcity FROM TEMPG)"],

18: ["CREATE TEMP TABLE TEMPH AS SELECT parts.pno, parts.pname, spj.sno FROM spj NATURAL JOIN parts", \
"CREATE TEMP TABLE TEMPI AS SELECT emp.last,emp.first,suppliers.sno FROM emp,suppliers WHERE emp.last = suppliers.sname", \
"CREATE TEMP TABLE TEMPJ AS SELECT first, last, pname FROM TEMPH NATURAL JOIN TEMPI WHERE (TEMPH.pname = 'Screw' OR TEMPH.pname ='Nut')", \
"SELECT * FROM TEMPJ ORDER BY first ASC, last ASC, pname ASC"],

19: ["SELECT * FROM parts WHERE color = 'Red' OR color = 'Green'"],
20: ["SELECT AVG(qty) AS goo FROM spj GROUP BY pno HAVING not(goo < 200) "],

21: ["CREATE TEMP TABLE TEMPK AS SELECT sno FROM suppliers WHERE suppliers.scity = 'Athens' OR suppliers.scity = 'Paris'", \
"CREATE TEMP TABLE TEMPL AS SELECT sno FROM suppliers WHERE sno not IN (SELECT sno FROM parts natural JOIN spj WHERE parts.pname = 'Cam')", \
"CREATE TEMP TABLE TEMPM AS SELECT TEMPL.sno FROM TEMPL INTERSECT SELECT * FROM TEMPK", \
"SELECT * FROM projects natural JOIN (SELECT jno FROM spj natural JOIN TEMPM WHERE spj.sno = TEMPM.sno)"],

22: ["SELECT projects.jno,jname,jcity FROM projects natural JOIN spj"],

23: ["CREATE TEMP TABLE TEMPN AS SELECT pcity FROM projects,parts WHERE projects.jcity = parts.pcity", \
"CREATE TEMP TABLE TEMPO AS SELECT jcity FROM suppliers,projects WHERE suppliers.scity = projects.jcity", \
"CREATE TEMP TABLE TEMPP AS SELECT scity FROM parts,suppliers WHERE parts.pcity = suppliers.scity", \
"SELECT pcity FROM TEMPN union SELECT jcity FROM TEMPO union SELECT scity FROM TEMPP"],

24: ["SELECT last FROM (SELECT * FROM emp WHERE not(city='Rome' OR city='Paris') union SELECT last,first,city,state,dept FROM emp,suppliers WHERE emp.last = suppliers.sname AND not(suppliers.scity = 'Rome' OR suppliers.scity = 'Paris')) ORDER BY last ASC"],
25: ["SELECT distinct jcity FROM projects WHERE jcity not IN (SELECT city FROM (SELECT last,first FROM mgr) natural JOIN emp)"],
26: ["SELECT * FROM projects natural JOIN (SELECT distinct jcity FROM projects WHERE jcity not IN (SELECT city FROM (SELECT last,first FROM mgr) natural JOIN emp))"],
27: ["SELECT * FROM parts WHERE pcity = 'London'  AND color = 'Red' ORDER BY pno"],
28: ["SELECT pname,qty FROM parts natural JOIN spj WHERE pcity ='London' AND color = 'Red' ORDER BY pname ASC, qty ASC"],
29: ["SELECT pcity,pname,weight,color FROM parts ORDER BY pcity ASC, pname ASC"],
30: ["SELECT * FROM parts,projects WHERE projects.jcity = parts.pcity ORDER BY pno ASC, weight ASC, pcity ASC, pno ASC"],
31: ["SELECT * FROM parts,projects WHERE projects.jcity = parts.pcity AND color ='Red' AND pname = 'Screw' ORDER BY pno ASC, jno ASC"],
32: ["SELECT * FROM suppliers, (SELECT * FROM projects, emp WHERE jcity = city) WHERE jcity=scity ORDER BY sno ASC, jno ASC"],
33: ["SELECT sno,first,last FROM suppliers,emp WHERE emp.last = suppliers.sname ORDER BY sno ASC"],
34: ["SELECT * FROM parts WHERE pcity ='London' OR pcity = 'Paris' OR pcity = 'New YORk' ORDER BY pno"],
35: ["SELECT distinct jno,jname,jcity FROM projects,(SELECT * FROM (SELECT last,first FROM mgr) natural JOIN emp) WHERE projects.jcity = city"],
36: ["SELECT pname,sname,jname,qty FROM suppliers natural JOIN (SELECT * FROM projects natural JOIN (SELECT * FROM spj natural JOIN parts))"],
37: ["SELECT * FROM mgr except SELECT last,first,dept FROM mgr natural JOIN emp"],
38: ["SELECT * FROM parts ORDER BY pno"],
39: ["SELECT projects.jno, jname, jcity FROM projects, suppliers, parts, spj WHERE projects.jno = spj.jno AND suppliers.sno = spj.sno AND parts.pno = spj.pno AND (projects.jcity = 'Athens' OR projects.jcity = 'Paris')"],
40: ["SELECT distinct pcity FROM parts,(SELECT jcity FROM projects,suppliers WHERE jcity = scity) WHERE pcity = jcity ORDER BY pcity"],
41: ["SELECT last FROM (SELECT * FROM emp WHERE NOT(city='Rome' OR city='Paris') union SELECT last,first,city,state,dept FROM emp,suppliers WHERE emp.last = suppliers.sname AND not(suppliers.scity = 'Rome' OR suppliers.scity = 'Paris')) ORDER BY last ASC"],
42: ["SELECT distinct jcity FROM projects WHERE jcity NOT IN (SELECT city FROM (SELECT last,first FROM mgr) natural JOIN emp)"],
43: ["SELECT pname,SUM(qty) FROM parts natural JOIN spj WHERE pcity='London' AND color='Red' group by pname ORDER BY pname ASC"],
44: ["SELECT pname, sum(qty) FROM parts natural JOIN spj WHERE pcity='London' group by pname ORder by pname ASC"],
45: ["SELECT pname, sum(qty) FROM parts natural JOIN spj WHERE pcity='London' AND color='Red' group by pname ORder by pname ASC"],

46: ["CREATE TEMP TABLE partsSPJ AS SELECT * FROM parts natural JOIN spj WHERE pname='Screw' OR pname='Nut'", \
"CREATE TEMP TABLE suppliersEMP AS SELECT * FROM emp, suppliers WHERE emp.last = suppliers.sname", \
"SELECT DISTINCT first, last, pname FROM suppliersEMP natural JOIN PartsSPJ ORDER BY last ASC, first ASC"],

47: ["CREATE TEMP TABLE theSuppliers AS SELECT first,last,sno FROM emp,suppliers WHERE emp.last=suppliers.sname", \
"CREATE TEMP TABLE theSPJs AS SELECT sno,pname FROM spj NATURAL JOIN parts WHERE pname = 'Nut' OR pname = 'Screw'", \
"SELECT DISTINCT first,last,pname FROM theSuppliers NATURAL JOIN theSPJs ORDER BY last ASC, first ASC, pname ASC"],

48: ["SELECT last FROM (SELECT last,city FROM emp union SELECT sname,scity FROM suppliers WHERE sname not IN (SELECT sname FROM emp,suppliers WHERE sname=last)) WHERE not(city='Rome' OR city='Paris') ORDER BY last ASC"],
49: ["SELECT last, city FROM (select last, city FROM emp, suppliers)"],

50: ["CREATE TEMP TABLE VAR1 AS SELECT pname,SUM(qty) AS newQty FROM parts NATURAL JOIN spj GROUP BY pname", \
"SELECT pname,newQty FROM VAR1 WHERE newQty>500 AND newQty<1500"],

51: ["CREATE TEMP TABLE VAR2 AS SELECT pcity,count(pcity) as pcityNum FROM parts group by pcity having pcityNum > 3", \
"SELECT jno,jname,jcity FROM VAR2,projects WHERE pcity = jcity ORDER BY jno"],

52: ["SELECT jno,jname,jcity,pno,pname,color,weight,pcity FROM projects LEFT OUTER JOIN parts ON projects.jcity=parts.pcity ORDER BY jno ASC, pno ASC"],
53: ["SELECT * FROM spj LEFT OUTER JOIN emp ON emp.dept=spj.qty"],
54: ["SELECT projects.jname, projects.jcity, projects.jno, sum(weight),count(spj.jno) FROM projects, parts, spj WHERE projects.jno = spj.jno AND spj.pno = parts.pno group by projects.jno"],
55: ["SELECT first, last FROM emp WHERE NOT(state='ut') AND dept = '200'"]
}

try:
    querySelect=int(raw_input('Enter a value [1-50] or 0 to exit: '))
except ValueError:
	print "Not a number"

while querySelect != 0:
	conn = sqlite3.connect(fileName)
	cur = conn.cursor()
	print "\n" + str(querySelect) + ":"
	printQuery(dict[querySelect])
	print "\n"
	try:
	    querySelect=int(raw_input('Enter a value [1-55] or 0 to exit: '))
	except ValueError:
		print "Not a number"
	conn.close()