import json
import os
import shutil
from datetime import datetime
import copy

"""
def copyToFile(fileName,directory, parameter, contents):

    sourceDir = "./BaseFiles"
    destDir = "./"+directory+"/" + str(parameter) +"_"+ str(contents[parameter])+"/"
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

    parameterFile = destDir+fileName    
    with open(parameterFile, "r") as file:
        data = json.load(file)
    
    for parameter in contents.keys():
        data[parameter] = contents[parameter]

    with open(parameterFile, "w") as file:
        file.write(json.dumps(data, indent=4))

    makefile = destDir+"makefile"

    with open(makefile, "w") as file:
        file.write("main:\n")
        file.write("\tjava -jar GATLAM.jar >> log.txt")

    return destDir

def copyToFile_param(fileName,directory, parameter, contents, parentParam):

    sourceDir = "./BaseFiles"
    destDir = "./"+directory+"/" + str(parameter) +"_"+ str(contents[parentParam][parameter])+"/"
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

    parameterFile = destDir+fileName    
    with open(parameterFile, "r") as file:
        data = json.load(file)
    
    for parameter in contents.keys():
        if(parameter in data[parentParam]):
            data[parentParam][parameter]["weight"] = contents[parameter]

    with open(parameterFile, "w") as file:
        file.write(json.dumps(data, indent=4))

    makefile = destDir+"makefile"

    with open(makefile, "w") as file:
        file.write("main:\n")
        file.write("\tjava -jar GATLAM.jar >> log.txt")

    return destDir


def createCombinations(data, paramName, defaultValues):
    otherValues = data[paramName]["othervalues"]
    
    combinations = []

    for val in otherValues:
        obj = {}
        for param in defaultValues.keys():
            obj[param] = defaultValues[param]
        obj[paramName] = val
        combinations = combinations + [obj]

    return combinations

def createCombinations_param(data, paramName, defaultValues, parentParam):
    otherValues = data[parentParam][paramName]["othervalues"]
    
    combinations = []

    for val in otherValues:
        obj = {}
        for param in data.keys():
            if(param == parentParam):
                innerObj = {}
                for innerParam in data[parentParam].keys():
                    innerObj[innerParam] =  defaultValues[innerParam]
                obj[param] = innerObj
            else:
                obj[param] = defaultValues[param]
        obj[parentParam][paramName] = val
        combinations = combinations + [obj]

    return combinations



def generate_json_files(data, filePath, current_date_str):
    parameters = data.keys();
    defaultValues = {}
    for param in parameters:
        defaultValues[param] = data[param]["defaultValue"]

    directories = []

    for param in parameters:
        combinations = createCombinations(data, param, defaultValues)
        for combination in combinations:
            directories = directories + [copyToFile(filePath,current_date_str, param, combination)]

    makefile = "makefile"

    with open(makefile, "a") as file:
        for dir in directories:
            file.write("\tmake -C" + dir + " || true\n")

def generate_json_files_param(data, filePath, current_date_str, parentParam):
    parentParameters = data.keys();
    parameters = data[parentParam].keys();
    defaultValues = {}
    for param in parameters:
        defaultValues[param] = data[parentParam][param]["defaultValue"]

    for param in parentParameters:
        if not(parentParam == param): 
            defaultValues[param] = data[param]["defaultValue"]

    directories = []

    for param in parameters:
        combinations = createCombinations_param(data, param, defaultValues, parentParam)
        for combination in combinations:
            directories = directories + [copyToFile_param(filePath,current_date_str, param, combination,parentParam)]

    makefile = "makefile"

    with open(makefile, "a") as file:
        for dir in directories:
            file.write("\tmake -C" + dir + " || true\n")

def makeModuleConfigurations(combination, directory, filePath):
    sourceDir = "./BaseFiles"
    destDir = "./"+directory+"/" + combination["Run name"]+"/"
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

    parameterFile = destDir+filePath   
    with open(parameterFile, "r") as file:
        data = json.load(file)
    
    for parameter in combination["Modules active"].keys():
        for module in data["modules"]:
            if module["moduleName"] == parameter:
                module["enabled"] = combination["Modules active"][parameter]
                break

    with open(parameterFile, "w") as file:
        file.write(json.dumps(data, indent=4))

    makefile = destDir+"makefile"

    with open(makefile, "w") as file:
        file.write("main:\n")
        file.write("\tjava -jar GATLAM.jar >> log.txt")

    return destDir

# Read the input JSON file
with open("experimentValues.json", "r") as f:
    data = json.load(f)

current_datetime = datetime.now()
current_datetime_str = current_datetime.strftime("%Y_%m_%d_%H_%M_%S")

makefile = "makefile"

with open(makefile, "w") as file:
    file.write("main:\n")

if "GA" in data:
    filePath = data["GA"]["filePath"]
    del data["GA"]["filePath"];
    if "FitnessFunction" in data["GA"]:
        generate_json_files_param(data["GA"], filePath, current_datetime_str, "FitnessFunction")
        del data["GA"]["FitnessFunction"]
    generate_json_files(data["GA"], filePath, current_datetime_str)

if "ModuleConfigurations" in data:
    filePath = data["ModuleConfigurations"]["filePath"]
    del data["ModuleConfigurations"]["filePath"]

    with open("makefile", "a") as f:
        for combination in data["ModuleConfigurations"]["combinations"]:
            makeCommand = makeModuleConfigurations(combination,current_datetime_str, filePath)
            f.write("\tmake -C \"" + makeCommand + "\" || true\n")

"""

def copyToFile(param, altValue, baseFolder):
    sourceDir = "./BaseFiles"
    destDir = "./"+baseFolder+"/" + str(param) +"_"+ str(altValue)+"/"
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

for EXPType in experimentValues.keys():
    defaultValues = {}
    alternativeValues = {}
    filePaths = ""

    innerDefaultValues = {}
    for key in experimentValues[EXPType].keys():
        if key == "filePath":
            filePaths = experimentValues[EXPType][key]
            continue
        for innerKey in experimentValues[EXPType][key].keys():
            if isinstance(experimentValues[EXPType][key][innerKey], dict):
                if not key in innerDefaultValues:
                    innerDefaultValues[key] = {}
                innerDefaultValues[key][innerKey] = experimentValues[EXPType][key][innerKey]["defaultValue"]
                alternativeValues[innerKey] = experimentValues[EXPType][key][innerKey]["othervalues"]

            else:
                innerDefaultValues[key] = experimentValues[EXPType][key]["defaultValue"]
                alternativeValues[key] = experimentValues[EXPType][key]["othervalues"]

    defaultValues = innerDefaultValues

    with open("./BaseFiles/" + filePaths, "r") as f:
        data = json.load(f)
        for key in data.keys():
            if not key in defaultValues:
                defaultValues[key] = data[key]

            elif isinstance(data[key], dict):
                if key == "FitnessFunction":
                    for subKey in data[key]:
                        temp = defaultValues[key][subKey]
                        del defaultValues[key][subKey]
                        defaultValues[key][subKey] = data[key][subKey]
                        defaultValues[key][subKey]["weight"] = temp
                        

    with open("makefile", "w") as f:
        f.write("main:\n")

    current_datetime = datetime.now()
    current_datetime_str = current_datetime.strftime("%Y_%m_%d_%H_%M_%S")


    for key in alternativeValues.keys():
        if key in defaultValues:
            for value in alternativeValues[key]:
                destDir = copyToFile(key, value, current_datetime_str)
                tempConfig = defaultValues.copy()
                tempConfig[key] = value

                with open(destDir + filePaths, "w") as file:
                    file.write(json.dumps(tempConfig, indent=4))

                with open("makefile", "a") as file:
                    file.write("\tmake -C " + destDir + " || true \n")

        else:
            for subKey in defaultValues.keys():
                if isinstance(defaultValues[subKey], dict) and key in defaultValues[subKey]:
                    for value in alternativeValues[key]:
                        destDir = copyToFile(key, value, current_datetime_str)
                        tempConfig = copy.deepcopy(defaultValues)
                        if(subKey == "FitnessFunction"):
                            tempConfig[subKey][key]["weight"] = value
                        else:
                            tempConfig[subKey][key] = value

                        with open(destDir + filePaths, "w") as file:
                            file.write(json.dumps(tempConfig, indent=4))

                        with open("makefile", "a") as file:
                            file.write("\tmake -C " + destDir + " || true \n")
