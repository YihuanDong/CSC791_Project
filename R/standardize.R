standardize <- function(data){
  #input: the class label is the last column of the data
  N = dim(data)[2]
  sd_zero = c()
  for(i in 1:(N-1)){
    mean_v = mean(data[[i]],na.rm = TRUE)
    index = is.na(data[[i]])
    data[[i]][index] = mean_v
    sd_v = sd(data[[i]])
    if(sd_v != 0){
      data[[i]] = (data[[i]] - mean_v)/sd_v
    }
    else{
      #record columns whose std is zero
      sd_zero = c(sd_zero,i)
    }
  }
  #remove columns with 0 std value
  if(length(sd_zero)>0){
    data = data[-sd_zero]
  }
  return(data)
}