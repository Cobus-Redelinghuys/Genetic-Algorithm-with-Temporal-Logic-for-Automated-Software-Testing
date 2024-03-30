import os
import json
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

directory_path = './'  # Replace this with the actual directory path

folder_names = [folder for folder in os.listdir(directory_path) if os.path.isdir(os.path.join(directory_path, folder))]

experimentNames = {}

print("Folder names in the directory:")
for folder_name in folder_names:
    print(folder_name)
    underscore_index = folder_name.find("_")
    if underscore_index != -1:
        result_string = folder_name[:underscore_index]
    if not result_string in experimentNames:
        experimentNames[result_string] = []
    value = folder_name[underscore_index+1:]
    experimentNames[result_string] += [value]

print(experimentNames)

#sns.set()
bigStats = {}

for keyType in experimentNames.keys():
    stats = {}
    for key in experimentNames[keyType]:
        try:
            with open("./" + keyType + "_" + key+ "/stat.json", "r") as f:
                stats[key] = json.load(f)
        except:
            print("File not found: " + "./" + keyType + "_" + key+ "/stat.json")

        try:
            with open("./" + keyType + "_" + key+ "/errorInfo.json", "r") as f:
                stats[key]["error"] = json.load(f)["error"]
        except:
            print("File not found: " + "./" + keyType + "_" + key+ "/errorInfo.json")
        
    
    bigStats[keyType] = stats

with open("cumulative_stats.json", "w") as f:
    f.write(json.dumps(bigStats))