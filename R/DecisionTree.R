DT_data = read.csv("Cond6_Final.csv", header = TRUE)
DT_data$predict = DT_data$hintFollow
DT_data$hintFollow = NULL
DT_data$hintFollow = DT_data$predict
DT_data$predict = NULL

#standarization
DT_data = standardize(DT_data)

N = dim(DT_data)[2]
sigFeature = FCBF(DT_data,0.1)
sigFeature = c(sigFeature,N)
DT_data = DT_data[sigFeature]


#Create sample training and testing data
numberOfRow = nrow(DT_data)
index <- sample(numberOfRow)
#training data
train_index <- index[1:(numberOfRow*0.7)]
X_train <- DT_data[train_index,]
#testing data
test_index <- index[(numberOfRow*0.7+1):numberOfRow]
X_test <- DT_data[test_index,]




N = dim(DT_data)[2]
#create formula
xnam <- paste0("", attributes(DT_data)$names[1:(N-1)])
(fmla <- as.formula(paste("hintFollow ~ ", paste(xnam, collapse= "+"))))


#decision tree
library("party")
dt_tree <- ctree(fmla, data=X_train)
#plot the decision tree
plot(dt_tree)


#calculate accuracy
prediction = predict(dt_ctree,X_test)
prediction[prediction<1] = 0

accuracy = sum((prediction == X_test$hintFollow))/nrow(X_test)
