dataPath <- "E:\\Courses\\CSC 791 Data Analysis for User Adaptive Systems\\CSC791_Project\\Data\\test.csv"
data <- read.csv(dataPath,header = TRUE)
dim(data)
train_data <- data[1:5,200:220]
test_data <- data[401:500,200:220]
glmData <- glm(hintFollow ~ ., data = train_data,family = binomial)
summary(glmData)
pre<-predict(glmData,newdata = test_data,type="response")
dim(pre)
pre[pre>0.5] = 1
pre[pre<0.5] = 0
count <- 0
for (i in 1:100){
  if (test_data$hintFollow[i]==pre[i]) {
    count <- count+1
  }
}
count
