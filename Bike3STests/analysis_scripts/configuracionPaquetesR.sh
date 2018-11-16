#!/bin/bash
##Hay que dar permisos al fichero: chmod 700 Script.sh

echo "Descargando paquetes necesarios..."
# ======================================
if [ "$(uname)" == "Darwin" ]; then
    # Do something under Mac OS X platform        
	brew install pandoc

elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    # Do something under GNU/Linux platform
	sudo apt-get update
	sudo apt-get install pandoc
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Do something under 32 bits Windows NT platform
	echo "Necesitas instalar PanDoc"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW64_NT" ]; then
    # Do something under 64 bits Windows NT platform
	echo "Necesitas instalar Pandoc"
fi
#======================================

sudo Rscript $1setup.R  # script R para instalar los paquetes necesarios 
