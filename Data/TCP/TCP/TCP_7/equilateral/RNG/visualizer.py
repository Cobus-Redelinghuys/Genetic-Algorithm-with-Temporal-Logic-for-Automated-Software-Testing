import json
import matplotlib.pyplot as plt
import numpy as np
import csv

with open('results.json') as json_file:
    data = json.load(json_file)

def drawGraph(data, xlabel, ylabel, title, pngName, ylim=False):
    x = np.arange(len(data))
    plt.figure()
    plt.plot(x, data)
    if ylim:
        plt.ylim(top=1)
    plt.ylim(bottom=0)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.title(title)
    plt.savefig(pngName)
    plt.close() 

def drawGraphLim(data, xlabel, ylabel, title, pngName, ylim: int):
    x = np.arange(len(data))
    plt.figure()
    plt.plot(x, data)
    plt.ylim(top=ylim)
    plt.ylim(bottom=0)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.title(title)
    plt.savefig(pngName)
    plt.close() 

def drawGraphDynamicX(yData, xData, xlabel, ylabel, title, pngName):
    y_ticks = np.arange(min(yData), max(yData)+1, 1)
    plt.figure(figsize=(10,7))
    plt.xticks(xData)
    plt.xticks(rotation='vertical')
    plt.yticks(y_ticks)
    plt.bar(xData, yData)

    plt.ylim(bottom=0)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.title(title)
    plt.savefig(pngName)
    plt.close() 

def generateTableForGenes(data):
    all_keys = set()
    for d in data:
        all_keys.update(d.keys())

    # Generate the LaTeX code for the table
    latex_table = "\\begin{tabular}{|c|" + "c|" * len(data) + "}\n"
    latex_table += "\\hline\n"
    latex_table += "Key & " + " & ".join([f"Gene {i+1}" for i in range(len(data))]) + " \\\\\n"
    latex_table += "\\hline\n"

    for key in sorted(all_keys):
        latex_table += str(key)
        for d in data:
            if key in d:
                latex_table += " & " + str(d[key])
            else:
                latex_table += " & 0"
        latex_table += " \\\\\n"

    latex_table += "\\hline\n"
    latex_table += "\\end{tabular}"

    with open("bestGenesTable.tex", "w") as f:
        f.write(latex_table)

def drawValuesTable(avg, std, best, var, duration):
    latexTable = "\\begin{longtable}{|l|l|l|l|l|l|}\n"
    latexTable += "\hline \n"
    latexTable += "Generation & Average Fitness & Standard Deviation & Best Fitness & Variation & Duration (ms) \n"
    latexTable += "\endfirsthead \hline \n"
    for i in range(len(avg)):
        latexTable += str(i+1) + " & " + str(avg[i]) + " & " + str(std[i]) + " & " + str(best[i]) + " & " + str(var[i]) + " & " + str(duration[i]) + " \\\\ \hline \n"

    latexTable += "\end{longtable}"

    with open("summaryTable.tex", "w") as file:
        file.write(latexTable)

def printGATLAMStats():
    std = []
    avg = []
    var = []
    best = []
    duration = []

    for entry in data['Results']:
        summary = entry['Summary']
        duration = duration + [summary['duration']]
        std = std + [summary['std']]
        avg = avg + [summary['avg']]
        var = var + [summary['var']]
        best = best + [summary['bestFitness']]

    drawGraph(avg, "Generation", "Average Fitness", "Average fitness per generation", "GATLAM_average.png", True)
    drawGraph(std, "Generation", "Standard Deviation", "Standard deviation on fitness values per generation", "GATLAM_standard_deviation.png")
    drawGraph(var, "Generation", "Variation", "The amount of variation in the population per generation", "GATLAM_variance.png", True)
    drawGraph(best, "Generation", "Best Fitness", "Best fitness value of each generation", "GATLAM_best.png", True)
    drawGraph(duration, "Generation", "Duration in seconds", "The duration to complete the generation", "duration.png")
    drawValuesTable(avg, std, best, var, duration)
    stats = {}
    stats["avg"] = avg
    stats["std"] = std
    stats["var"] = var
    stats['best'] = best
    stats["duration"] = duration
    with open("stat.json", "w") as f:
        f.write(json.dumps(stats))

printGATLAMStats()

equilateral = "equilateral"
isosceles = "isosceles"
scalene = "scalene"
not_a_triangle = "not a triangle"

def calculateErrors(chromosome):
    with open("./Config.json") as f:
        config = json.load(f)
    interpretorResults = chromosome["InterpretorResults"]
    numErrors = 0
    for result in interpretorResults:
        word = config["FitnessFunction"]["IllegalOutput"]["words"][0]
        if word in result["stdOut"]:
            numErrors += 1

    return numErrors

def calculateManualStats():
    std = []
    avg = []
    var = []
    best = []
    errors = []
    bestChromosomes = []
    for entry in data['Results']:
        details = entry['Detailed']
        fitnesses = []
        representations = []
        bestFitness = -1
        bestChrom = 0
        for chromosome in details["Chromosomes"]:
            fitnesses = fitnesses + [float(chromosome["fitness"])]

            representations = representations + [str(chromosome["bits"])]

            if float(chromosome["fitness"]) > bestFitness:
                bestFitness = float(chromosome["fitness"])
                bestChrom = chromosome
        
        avg = avg + [np.average(fitnesses)]
        std = std + [np.std(fitnesses)]
        var = var + [len(set(representations)) / len(representations)]
        best = best + [bestFitness]
        bestChromosomes = bestChromosomes + [bestChrom]
        errors = errors + [calculateErrors(bestChrom)]

    drawGraph(avg, "Generation", "Average Fitness", "Average fitness per generation", "MANUAL_average.png")
    drawGraph(std, "Generation", "Standard Deviation", "Standard deviation on fitness values per generation", "MANUAL_standard_deviation.png")
    drawGraph(var, "Generation", "Variation", "The amount of variation in the population per generation", "MANUAL_variance.png")
    drawGraph(best, "Generation", "Best Fitness", "Best fitness value of each generation", "MANUAL_best.png")
    drawGraphLim(errors, "Generation", "Number of errors", "Number of errors per generation", "errors.png", 10)
    with open("errorInfo.json", "w") as f:
        j = {}
        j["error"] = errors
        f.write(json.dumps(j))

    geneCounts = []
    for i in range(len(bestChromosomes[0]["genes"])):
        geneCounts = geneCounts + [{}]
    for chromosome in bestChromosomes:
        for i in range(len(chromosome["genes"])):
            if chromosome["genes"][i] in geneCounts[i]:
                geneCounts[i][chromosome["genes"][i]] = geneCounts[i][chromosome["genes"][i]] + 1
            else:
                geneCounts[i][chromosome["genes"][i]] = 1

    for i in range(len(geneCounts)):
        xdata = []
        ydata = []
        for key in geneCounts[i]:
            xdata = xdata + [key]
            ydata = ydata + [geneCounts[i][key]]

        combined_data = list(zip(xdata, ydata))
        sorted_data = sorted(combined_data, key=lambda x: x[0])
        xdata = [x for x, _ in sorted_data]
        ydata = [y for _, y in sorted_data]

        drawGraphDynamicX(ydata, xdata, "Gene values", "Number of occurences", "Number of occurences of gene values in best chromosomes for gene " + str(i), "gene_" + str(i) + "_occurences.png")
    
    generateTableForGenes(geneCounts)

    with open("bestChromosomeOutput.json", "w") as f:
        f.write(json.dumps(bestChromosomes))

    
calculateManualStats()

def TCP(side1, side2, side3):
    if side1 + side2 > side3 and side2 + side3 > side1 and side1 + side3 > side2:
        if side1 == side2 == side3:
            return "Equilateral Triangle"
        elif side1 == side2 or side2 == side3 or side1 == side3:
            return "Isosceles Triangle"
        else:
            return "Scalene Triangle"
    else:
        return "Not a Triangle"
    
with open("final_popuation.json", "r") as f:
    final_pop = json.load(f)

trig_stats = {}

for pop in final_pop:
    data = pop["Input"]
    trig = TCP(data[0], data[1], data[2])

    if not trig in trig_stats:
        trig_stats[trig] = 0

    trig_stats[trig] = trig_stats[trig] + 1

with open("trig_results.json", "w") as f:
    f.write(json.dumps(trig_stats))
    
best_trigs = {}
with open("bestChromosomeOutput.json") as f:
    best_trigs = json.load(f)
    
trig_stats = {}
for chromosome in best_trigs:
    bits = chromosome["bits"]
    failed = chromosome["fitnessBreakdown"]["IllegalOutput"]
    if failed > 0:
        if not bits in trig_stats:
            trig_stats[bits] = 0
        trig_stats[bits] += 1
        
with open("trig_results2.json", "w") as f:
    f.write(json.dumps(trig_stats))