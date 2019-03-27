import sys
import json

def jsonDefault(object):
    return object.__dict__


with open(sys.argv[1]) as filename:
    data = json.load(filename)
    for x in data["entryPoints"]:
        x["totalUsers"] = int(sys.argv[2])

    with open(str(sys.argv[1]), "w") as outfile:
        json.dump(data, outfile, default=jsonDefault, indent=4)

