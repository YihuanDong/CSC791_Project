FCBF <- function(data,threshold=0.5){
  #input: Data Frame, make sure the last column is 
  #the attribute needed to be predict
  N = length(data) - 1
  potential = c()
  corValue = c()
  for(i in 1:N){
    #check whether this column's std is zero
    if(sd(data[[i]],na.rm = TRUE) == 0)next;
    #use mean value to replace NA value
    index = is.na(data[[i]])
    data[[i]][index] = mean(data[[i]],na.rm = TRUE)
    
    #calculate correlation between current feature and class label
    correlation = abs(cor(data[[i]],data[[N+1]],use = "complete.obs"))
    
    if(correlation >= threshold){
      potential = c(potential,i)
      corValue = c(corValue,correlation)
    }
  }

  #sort potential features in descending value
  sort_index = order(corValue, decreasing = TRUE)
  potential = potential[sort_index]
  
  #get first element in potential feature list
  Fp = potential[1]
  indexFp = 1
  
  #repetition
  repeat{
    #get next element
    Fq = potential[indexFp+1]
    indexFq = indexFp + 1
    
    if(!is.na(Fq)){
      repeat{
        Fqn = Fq;
        indexFqn = indexFq
        if(abs(cor(data[[Fp]],data[[Fq]],use = "complete.obs"))>=abs(cor(data[[Fq]],data[[N+1]],use = "complete.obs"))){
          #remove Fq from potential list
          potential = setdiff(potential,Fq)
          
          #get next element
          Fq = potential[indexFqn+1]
          indexFq = indexFqn+1
        }
        else{
          #get next element
          Fq = potential[indexFq+1]
          indexFq = indexFq + 1
        }
        if(is.na(Fq)){
          break;
        }
      }
    }
    #get next element
    Fp = potential[indexFp+1]
    indexFp = indexFp + 1
    if(is.na(Fp)){
      break;
    }  
  }
  
  goodFeatures = potential
  print(goodFeatures)
  
  #attributes(data)$names[goodFeatures]
  return(goodFeatures)
}
