#load data
inputFilePath <- "E:\\Courses\\CSC 791 Data Analysis for User Adaptive Systems\\CSC791_Project\\Data\\Cond6\\Cond6_Ready.csv"
data <- read.csv(inputFilePath)

#data cleaning
#data <- dataCleansing(data)

#output generation
output <- generateOutputData(data)

#write data
outputFilePath <- "E:\\Courses\\CSC 791 Data Analysis for User Adaptive Systems\\CSC791_Project\\Data\\Cond6\\Cond6_Tag.csv"
write.csv(output,file = outputFilePath)
