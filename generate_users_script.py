import sys
import json

def jsonDefault(object):
    return object.__dict__


with open(sys.argv[1]) as filename:
    data = json.load(filename)
    for x in data["entryPoints"]:
        x["distribution"]["lambda"] = float(sys.argv[2])
    filename.close()

    with open(str(sys.argv[1]), "w") as outfile:
        json.dump(data, outfile, default=jsonDefault, indent=4)

