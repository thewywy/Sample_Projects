# Grocery Inventory Manager

# By Wyatt Sorenson

# Requirements: Python and browser

# How to use: (e.g. python Grocery_list_sorting.py glist.json grocery.html)

# This program utilized JSON objects that contains a list of departments and a dicitonary of grocery items.
# It sorts the items by department and lists:
# All Items
# Active Items
# Out of Stock Items
# Overstocked Items
# Inventory Overhead

import sys
import  json

jsonObject = json.load(open(sys.argv[1]))

# Sorting list by sorted function
ListSorted = sorted(jsonObject["itemList"], key = lambda k: jsonObject["storeOrder"].index(k["section"]))
print "Sorting Successful!"

def printTable(listFilter, listName):
	outputObject.write("<h3>" + listName + ":</h5>\n<table>\n<tr><th>Name</th><th>Contents</th><th>Dept</th><th>Notes</th></tr>\n")
	for item in listFilter:
		outputObject.write("<tr>")
		outputObject.write("<td>{0:15}</td><td>{1:15}</td><td>{2:15}</td><td>{3:15}</td>".format(item["name"], item["amountStr"],item["section"], item["notes"]))
		outputObject.write("</tr>\n")
	outputObject.write("</table>\n\n")

def overhead():
	inventoryOverhead = 0.0
	for item in ListSorted:
		inventoryOverhead = (item["price"] * item["amountNumber"]) + inventoryOverhead
	outputObject.write("<h3>Inverntory Overhead:</h3>")
	outputObject.write("<p>${0:,.2f}</p>".format(inventoryOverhead))


# Generating html
outputObject = open(sys.argv[2], "w+")
outputObject.write("<!DOCTYPE html>\n<html><head>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"></head>\n<body>\n<h2>Grocery Management Report\n\n")

# All items in the system
printTable(ListSorted, "All")

# Filtering "active" by list comprehension and generating table
activeFiltered = [x for x in ListSorted if x["status"] != 0]
printTable(activeFiltered, "Currently Active")

# Filtering "Reorder" by list comprehension
reorderFiltered = [x for x in ListSorted if x["status"] != 0 and x["amountNumber"] < 1]
printTable(reorderFiltered, "Out of Stock/Reorder")

# Filtering "Overstock/Discount for quick sale" by list comprehension
overstockFiltered = [x for x in ListSorted if x["status"] != 0 and x["amountNumber"] > 100]
printTable(overstockFiltered, "Overstock/Discount")

# Overhead
overhead()

# Closing html
outputObject.write("</body>\n</html>")

print "Open grocery.html to see results."
outputObject.close()