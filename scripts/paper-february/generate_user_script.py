import sys
import json


with open(sys.argv[1]) as filename:
    data = json.load(filename)
    print(data)
