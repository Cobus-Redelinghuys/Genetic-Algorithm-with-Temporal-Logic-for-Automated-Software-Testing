import json
import os
import shutil
from datetime import datetime

def copyToFile(param, baseFolder):
    sourceDir = "./BaseFiles"
    destDir = "./"+baseFolder+"/" + str(param) + "/"
    files = os.listdir(sourceDir)

    if(os.path.exists(destDir)):
        shutil.rmtree(destDir)

    os.makedirs(destDir)

    for file in files:
        sourcePath = os.path.join(sourceDir, file)
        destPath = os.path.join(destDir, file)

        if os.path.isfile(sourcePath):
            shutil.copy(sourcePath, destPath)
        elif os.path.isdir(sourcePath):
            shutil.copytree(sourcePath, destPath)

    with open(destDir + "makefile", "w") as file:
        file.write("main:\n")
        file.write("\tjava -jar GATLAM.jar >> log.txt")

    return destDir

with open("experimentValues.json", "r") as f:
    experimentValues = json.load(f)

current_datetime = datetime.now()
current_datetime_str = current_datetime.strftime("%Y_%m_%d_%H_%M_%S")

with open("makefile", "w") as f:
    f.write("main:\n")

for EXPType in experimentValues.keys():
    filePath = experimentValues[EXPType]["filePath"]
    del experimentValues[EXPType]["filePath"]
    if EXPType == "ModuleConfigurations":
        for TestID in experimentValues[EXPType]["combinations"]:
            dir = copyToFile(TestID["Run name"], current_datetime_str)
            with open(dir + filePath, "r") as f2:
                moduleConfig = json.load(f2)

            for module in TestID["Modules active"].keys():
                for realModule in moduleConfig["modules"]:
                    if realModule["moduleName"] == module:
                        realModule["enabled"] = TestID["Modules active"][module]
                        continue

            with open(dir + filePath, "w") as f2:
                f2.write(json.dumps(moduleConfig, indent=4))

            with open("makefile", "a") as file:
                    file.write("\tmake -C \"" + dir + "\" || true \n")
            