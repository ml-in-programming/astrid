# astrid
An IntelliJ IDEA plugin that allows to get suggestions for better method names based on state-of-art machine learning approach [code2seq](https://github.com/tech-srl/code2seq). The goal of the plugin is to improve code quality. Currently plugin supports only Java projects. 

The plugin contains inspections that analyzes methods in the project in background mode, supplies them to the code2seq as input, processes result predictions and shows suggestions if there are better names for the method.

## Inspections list
They are accessed via ```Alt+Enter``` when the cursor is at an method name or if-statement.
* ```Method names suggestions```
  * Inspection is intended for suggesting better method names for methods in the project. If user accepts suggestion plugin does rename refactoring for all method occurrences. 
* ```If-statement extractor```
  * Inspection is intended for detecting long if-statement conditions. Inspection extracts condition's body, creates new method in the current class, put the condition's body into new method, generates name for the it and adds method call to the if-statement condition.

## Installation
Prerequisites
* Currently plugin is available only for **Linux** and **Mac**

### Install from disk (need the .jar)
* Build .jar ```gradle jar``` 
* Go to ```Settings-> Plugins-> Install plugin from disk```
* Locate and select result .jar from the first step
* Restart IntelliJ IDEA

## How to use
### Generate suggestions by request
Click on the method name, tap ```Alt+Enter``` and choose ```Generate suggestions```
### Automate code inspection
Inspection highlights some method names, you can click on method name, tap ```Alt+Enter``` and choose ```Get method name suggestions```. You can approve one suggestion or ignore. 
Also you can suppress inspection's tip on the method by selecting "Suppress on this method". 

![](https://github.com/ml-in-programming/astrid/blob/master/gifs/astrid_inspection.gif)
