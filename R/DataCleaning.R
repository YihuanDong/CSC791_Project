#data cleaning function
dataCleansing<-function(data){
  #input: Deep Thought Raw Data
  
  #clean columns
  data$course <- NULL
  data$guest <- NULL
  data$module <- NULL
  data$tCond <- NULL
  data$selfExplain <- NULL
  data$message <- NULL
  data$dateTime <- NULL
  
  
  data$RLpolicy <- NULL
  data$RLpolicyType <- NULL
  data$IMMED_TotalPSTime <- NULL
  data$IMMED_WrongApp <- NULL
  data$IMMED_CumTotalWETime <- NULL
  data$IMMED_CumUseCount <- NULL
  data$IMMED_CumAppCount <- NULL
  data$IMMED_NextProblem_NumProbRule <- NULL
  data$IMMED_NewLevel <- NULL
  
  data$DELAY_stepTimeDeviation <- NULL
  data$DELAY_probDiff <- NULL
  data$DELAY_CumSymRepCount <- NULL
  data$DELAY_CumActionCount <- NULL
  data$DELAY_CumSystemInfoHintCount <- NULL
  data$DELAY_CumNextStepClickCountWE <- NULL
  
  #delete rows:
  #delete worked example rows
  data <- data[data$problemType != "WE",]
  
  #===delete useless actions
  #Click on empty area -1
  data<-data[data$action != -1,]
  #Rule Description Button Press 4
  #data<-data[data$action != 4,]
  #Instructions Button Pressed 11
  data<-data[data$action != 11,]
  #Window Information Button Pressed 12
  data<-data[data$action != 12,]
  #Contact Version Information 13
  data<-data[data$action != 13,]
  #Representation Switch 14
  data<-data[data$action != 14,]
  
  #===delete rows with errors
  #System failure (thrown/caught exception)
  data<-data[data$error != -1,]
  #Rule requires one justified premises 1
  #data<-data[data$error != 1,]
  #Rule requires two justified premises 2
  #data<-data[data$error != 2,]
  #Attempt to apply rule to conclusion 3
  data<-data[data$error != 3,]
  #Attempt to change proof direction mid-proof 4
  data<-data[data$error != 4,]
  #Attempt to delete nothing or trying to delete >1 blocks 5
  data<-data[data$error != 5,]
  #No hint available 6
  data<-data[data$error != 6,]
  #Form/Instantiation Validation Error, try again 7
  data<-data[data$error != 7,]
  #Redundant rule application 8
  #data<-data[data$error != 8,]
  #Block exists and already has parents 9
  data<-data[data$error != 9,]
  #Incorrect rule application 10
  data<-data[data$error != 10,]
  #Tried to apply rule to underived block but did not use ‘?’ button 11
  data<-data[data$error != 11,]
  #When the user applied a rule but did not enter anything into the prompt box 12
  #data<-data[data$error != 12,]
  
  #remove change direct/indirection proof; remove end; remove rule with #NAME? or login
  #data<-data[data$action != 8,]
  #data<-data[data$action != 98,]
  data<-data[data$rule != "login",]
  data<-data[data$rule != "#NAME?",]
  data<-data[!(data$preState=="" | data$postState=="undefined"),]
  
  #remove hint with content 'undefined' and 'no hint available'
  badHint<-c("undefined", "No hint available for this step.")
  data<-data[!data$hintGiven %in% badHint,]
  
  #remove duplicate hint request on the same state
  #data<-data[!(data$rule %in% "get" & duplicated(data[,c("rule", "preState", "currPrb","hintGiven")])),]
  
  #remove data with collaborators
  #data<-data[!data$collaborators=="",]
  return (data)  
}

#load data
inputFilePath <- "E:\\Courses\\CSC 791 Data Analysis for User Adaptive Systems\\CSC791_Project\\Data\\Cond5\\Cond5_Original.csv"
data <- read.csv(inputFilePath)

#clean data
data<-dataCleansing(data)

#write data
outputFilePath <- "E:\\Courses\\CSC 791 Data Analysis for User Adaptive Systems\\CSC791_Project\\Data\\Cond5\\Cond5_Clean.csv"
write.csv(data,file = outputFilePath)