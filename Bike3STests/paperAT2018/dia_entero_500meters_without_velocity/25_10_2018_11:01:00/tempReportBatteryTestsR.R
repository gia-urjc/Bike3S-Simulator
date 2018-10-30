
setwd("./../../../../IntellijProjects/Bike3STests/paperAT2018/dia_entero_500meters_without_velocity/25_10_2018_11:01:00/")


listcsv <- list.files(path = "./analisis", 
                      pattern = "users.csv", #an optional regular expression. Only file names which match the regular expression will be returned.
                      #pattern = "*.csv" # creates the list of all the csv files in the directory
                      all.files = FALSE, # If FALSE, only the names of visible files are returned. If TRUE, all file names will be returned.
                      full.names = TRUE, #If TRUE, muestra toda la ruta. Si no muestra sólo el nombre de la carpeta
                      recursive = TRUE,#Should subdirectory names be included in recursive listings?
                      ignore.case = FALSE, 
                      include.dirs = FALSE, 
                      no.. = FALSE)

#-----------------------
mi_vector <- c("barcelona","tarragona","lerida","gerona") 
mi_vector2 <- c("barcelona1","tarragona1","lerida1","gerona1") 

mi_vector2 <- t(mi_vector2)
mi_vector 
View(mi_vector )


mi_vector <- t(mi_vector)
View(mi_vector )

mi_vector <-  rbind(mi_vector, mi_vector2)
View(mi_vector)

#-----------------------


#tabla<-NULL
#tabla<-data.frame(t(c("Test","Media")))
tabla <-data.frame()
tabla
View(tabla)
#tabla <- as.vector("","")
listcsv
for (k in 1:length(listcsv)){
  print(listcsv[k])
  
  # Leo fichero
  users  <- read.csv(listcsv[k],
                     header = TRUE,
                     sep = ";"
  )
  # Elimino las filas que tienen la celda en blanco
  timeToStationAux <- users$time.to.origin.station[!is.na(users$time.to.origin.station)]
  
  #Creo una columna
  timeToStation <-  t(c(listcsv[k], mean (timeToStationAux)))
  
  
  #Añado a la tabla las columnas
  #(dat<-cbind(dat,bins2))
  (tabla<-rbind(tabla,timeToStation))
  #View(tabla)
  print("-----------")
}  
#tabla <- t(tabla)
#names(timeByUser)[2] <- "time"
names(tabla)[1] <- "time"
names(tabla) <- c("laa","sertd")
View(tabla)

str(tabla)
str(users)
tabla <- sapply(tabla,function(x) {x <- gsub("analisis","lala",x)})

tabla[] <- lapply(tabla[], gsub, pattern = "analisis", replacement = "lala", fixed = TRUE)

tabla
View(tabla)


tabla2 <- data.frame(tabla)

tabla2
View(tabla2)
View (c(tabla[,1]))
t(tabla[,2])


listcsv <- list.files(path = "./analisis",
                      pattern = "global_values.csv", #an optional regular expression. Only file names which match the regular expression will be returned.
                      all.files = FALSE, # If FALSE, only the names of visible files are returned. If TRUE, all file names will be returned.
                      full.names = TRUE, #If TRUE, muestra toda la ruta. Si no muestra sólo el nombre de la carpeta
                      recursive = TRUE,#Should subdirectory names be included in recursive listings?
                      ignore.case = FALSE, 
                      include.dirs = FALSE, 
                      no.. = FALSE)
listcsv
# Creo los dataFrames necesarios:
tablaStationBalancingQuality <-data.frame()


for (k in 1:length(listcsv)){
  # Leo fichero
  valores  <- read.csv(listcsv[k],
                     header = TRUE,
                     sep = ";"
  )
  Aux <-  t(c(valores$Demand.satisfaction, valores$Hire.eficiency, valores$Return.eficiency))
  #Añado a la tabla las columnas
  (tablaStationBalancingQuality<-rbind(tablaStationBalancingQuality,Aux))
}

tablaStationBalancingQuality
