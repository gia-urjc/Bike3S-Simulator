#!/bin/bash
##Hay que dar permisos al fichero: chmod 700 FirstScript

#Compruebo el número de argumentos
if [ $# -eq 3 ] 
then
# -----------------------------------------------------
	if [ -z "$RSTUDIO_PANDOC" ]; then
		echo "Configuring... RSTUDIO_PANDOC"
		export RSTUDIO_PANDOC=/Applications/RStudio.app/Contents/MacOS/pandoc 
		#Ojo, la variable aquí se configura para el script, luego se destruye al acabar el script. Si se crea fuera del script se mantiene hasta que se cierre la ventana del bash.
	else
		echo "RSTUDIO_PANDOC configured!"
	fi

	echo "Generating reportMarkdown..."
	Rscript -e "rmarkdown::render('$1/ReportBatteryTest.Rmd', output_file = '$2',params = list(path = '$3'))"

# -----------------------------------------------------

else
	echo "Error in the number of arguments"
fi 
