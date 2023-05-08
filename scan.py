#!/usr/bin/python3
n = 7
total = 0
collector = 4
total_without_collector = 0
for i in range(0, n):
    val = 0
    f = open("ab/error/servent"+str(i)+"_err.txt", "r")
    lines = f.readlines()

    for line in lines:
        a = line.split(" ")
        b = a[2].split("_") 
        if b[0].strip() == "TOKEN":
            break
        if b[1] == "received":
            val = val + int(b[0])
        elif b[1] == "sent":
            val = val - int(b[0])
    print("Servent {}: val = {}".format(i, val))
    total = total + val + 1000
    if i != collector:
        total_without_collector = total_without_collector + val + 1000
    else:
        total_without_collector = total_without_collector + 1000
print("Total bitcakes, excluding those in channels: " + str(total))
print("Total bitcakes, exluding those in channels and the collector ndoe: " + str(total_without_collector))
