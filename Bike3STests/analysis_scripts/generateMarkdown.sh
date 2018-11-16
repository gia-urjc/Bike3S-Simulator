#!/bin/bash
##Hay que dar permisos al fichero: chmod 700 FirstScript


pwd
#Compruebo el número de argumentos
if [ $# -eq 3 ] 
then
# -----------------------------------------------------	
	if [ -z "$RSTUDIO_PANDOC" ]; then
		echo "Configuring... RSTUDIO_PANDOC"
		if [ "$(uname)" == "Darwin" ]; then
		    # Do something under Mac OS X platform        
			echo "Configuro para mac"
			export RSTUDIO_PANDOC=/Applications/RStudio.app/Contents/MacOS/pandoc
		elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
		    # Do something under GNU/Linux platform
			echo "Configuro para linux..."
			export RSTUDIO_PANDOC=/usr/lib/rstudio/bin/pandoc
		elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
		    # Do something under 32 bits Windows NT platform
			echo "Configuro para win 32bit"
		elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW64_NT" ]; then
		    # Do something under 64 bits Windows NT platform
			
			echo "Configuro para win 64bits"
		fi
 
		#Ojo, la variable aquí se configura para el script, luego se destruye automáticamente al acabar el script. Si se crea fuera del script se mantiene hasta que se cierre la ventana del bash.
	else
		echo "RSTUDIO_PANDOC configured!"
	fi

	echo "Generating reportMarkdown..."

	echo "$1/ReportBatteryTest.Rmd"
	echo "$2"
	echo "$3"


	Rscript -e "rmarkdown::render('$1/ReportBatteryTest.Rmd', output_file = '$2',params = list(path = '$3'))"

# -----------------------------------------------------

else
	echo "Error in the number of arguments"
fi 
