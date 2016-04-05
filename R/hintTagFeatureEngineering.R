library(plyr)

dataCleansing<-function(data){
  
  #remove change direct/indirection proof; remove end; remove rule with #NAME? or login
  data<-data[data$action != 8,]
  data<-data[data$action != 98,]
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

generateOutputData<-function(data){
  output<-data.frame("")
  student<-unique(data[,"studentID"],)
  count <- 0
  for (s in student){ #for each student
    studentData<-data[data$studentID == s,]
    problem<-unique(studentData[,"currPrb"])
    for (p in problem){ #for each problem
      studentProblemData<-studentData[studentData$currPrb == p,]
      r<-1
      while (r<=nrow(studentProblemData)){ 
        studentProblemHintDataSize<-0
        if (studentProblemData[r, "hintGiven"] != ""){
          #for each presented lvl1 hint, call for stats
          if (grepl("Try to derive",studentProblemData[r, "hintGiven"])){ 
            count <- count+1
            row<-data.frame("")
            row<-cbind(lvl1Hint=as.character(studentProblemData[r, "hintGiven"]), studentID=as.character(studentProblemData[r, "studentID"]),
                       currPrb=as.character(studentProblemData[r, "currPrb"]), state=as.character(studentProblemData[r, "preState"]),
                       elTime =as.character(studentProblemData[r,"elTime"]))
            studentProblemHintData<-generateNext5StateActions(studentProblemData, r)
            temp<-engineerNewFeatures(studentProblemHintData)
            row<-cbind(row, temp)
            output<-rbind.fill(output, row)
            #print(s)
            #print(p)
            #r<-r+nrow(studentProblemHintData)-1
          }
        }
        r<-r+1
      }
    }
  }
  print(count)
  return (output)
}

#return how many levels of hints are presented, and call for stats on the first lvl hint
engineerNewFeatures<-function(studentProblemHintData){
  hints<-unique(studentProblemHintData[studentProblemHintData$hintGiven!="",]$hintGiven)
  levelsPresented<-1
  if (length(hints)>=4 && grepl("and click on rule", hints[4])){
    levelsPresented<-4
  }
  else if (length(hints)>=3 && grepl("Click on rule", hints[3])){
    levelsPresented<-3
  }
  else if (length(hints)>=2 && grepl("to derive it", hints[2])){
    levelsPresented<-2
  }
  row<-cbind(levelsPresented=levelsPresented, mainHintFollowingStats(studentProblemHintData, as.character(hints[1])))
  return (row)
}


########## major functions #############
#return whether a 1st lvl hint is followed, and call for other stats if followed
#input: student action data should only contins actions from hint to the next 5 consequetive state change
mainHintFollowingStats<-function(studentProblemHintData, hint){
  hintedRule<-gsub("Try to derive ", "", hint)
  hintedRule<-gsub(" working forward.", "", hintedRule)
  hintedRule<-gsub(" working backward.", "", hintedRule)
  if (nchar(hintedRule)<=2){
    hintedRuleStates<-paste(hintedRule, "[",sep="")
  }
  else{
    hintedRuleStates<-paste(hintedRule, ")","[",sep="")
  }
  statesSeq<-studentProblemHintData[cumsum(rle(as.numeric(studentProblemHintData$postState))$lengths),]
  #print(hintedRuleStates)
  stateChange<-which(grepl(regexEscapeChar(hintedRuleStates), statesSeq$postState))
  if (length(stateChange) !=0){ #hint is followed
    row<-data.frame(lvl1HintFollowed=TRUE, stateChangedB4FollowHint=stateChange[[1]]-1)
    print(stateChange[[1]]-1)
    row<-cbind(row, calcMainHintFollowingStats(studentProblemHintData,
                                               which(grepl(regexEscapeChar(hintedRuleStates), studentProblemHintData$postState))[[1]]))
  }
  else{ #hint is not followed
    row<-data.frame(lvl1HintFollowed=FALSE)
  }
  return (row)
}


#return stats when a hint is followed
#different rules applied, different nodes highlighted, nodes derived, number of restart
calcMainHintFollowingStats<-function(studentProblemHintData,hintFollowedRowNum){
  hintStartRow<-studentProblemHintData[1,]
  hintFollowedRow<-studentProblemHintData[hintFollowedRowNum,]
  #restartedRow<-studentProblemHintData(which(studentProblemHintData$action==9))
  timeCost<-hintFollowedRow$elTime-hintStartRow$elTime
  
  subData<-studentProblemHintData[1:hintFollowedRowNum,]
  #subData2<-studentProblemHintData[hintStartRow:restartedRow-1,]
  
  diffRulesApplied<-nrow(subData[subData$action==3 &!duplicated(subData[,c("rule", "application")]),])  
  diffNodesHighlighted<-nrow(subData[subData$action==1 & !duplicated(subData[,c("rule")]),])
  nodesDerived<-nrow(subData[subData$derived!="null" & !duplicated(subData[,c("derived")]),])  
  numRestart<-nrow(subData[subData$action==9,])
  numDiffNodeInResult<-getNumNodes(hintFollowedRow[1,"postState"])-getNumNodes(hintStartRow[1,"postState"])
  
  row<-data.frame(cbind(lvl1_timeCost = timeCost, diffRulesApplied=diffRulesApplied, diffNodesHighlighted=diffNodesHighlighted,
                         nodesDerived=nodesDerived,numRestart=numRestart,numDiffNodeInResult=numDiffNodeInResult))
  return (row)
}



######### helper functions ############
getNumNodes<-function(state){
  regex<-paste("\\]","\\,", sep="")
  return (length(gregexpr(regex, state)[[1]]))
}

#extract action data from first level hint to the next 5 state actions
generateNext5StateActions<-function(studentProblemData, hintRow){
  states<-unique(studentProblemData[hintRow:nrow(studentProblemData),]$postState)
  if (length(states) <= 5){
    lastState<-states[length(states)]
  }
  else{
    lastState<-states[5]  #maybe until the end of problem
  }
  rowLastState<-which(studentProblemData$postState == lastState)
  return (studentProblemData[hintRow:rowLastState,])
}

#regex replace
regexEscapeChar<-function(regex){
  regex<-gsub("\\+","\\\\+", regex)
  regex<-gsub("\\-","\\\\-", regex)
  regex<-gsub("\\*","\\\\*", regex)
  regex<-gsub("\\(","\\\\(", regex)
  regex<-gsub("\\)","\\\\)", regex)
  regex<-gsub("\\]","\\\\]", regex)
  regex<-gsub("\\[","\\\\[", regex)
  return (regex)
}
