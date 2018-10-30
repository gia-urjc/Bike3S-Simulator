#!/bin/bash

##Hay que dar permisos al fichero: chmod 700 FirstScript
echo ---------------------

if [ -z "$RSTUDIO_PANDOC" ]; then
	echo "Configuring... RSTUDIO_PANDOC"
	export RSTUDIO_PANDOC=/Applications/RStudio.app/Contents/MacOS/pandoc 
	#Ojo, la variable aquí se configura para el script, luego se destruye al acabar el script. Si se crea fuera del script se mantiene hasta que se cierre la ventana del bash.
else
 echo "RSTUDIO_PANDOC configured!"
fi

echo "Generating reportMarkdown..."
Rscript -e "rmarkdown::render('ReportBatteryTest.Rmd', params = list(path = './analisis'))"
