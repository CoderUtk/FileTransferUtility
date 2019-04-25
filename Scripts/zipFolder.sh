#!/bin/bash
cd $HOME
source="/home/cloud_as1/users/utkarsh/TXTs"
cd $source/../
foldername=$(basename $source)
if [[ -d $source ]]; then
	echo "Zipping "$foldername " to $foldername.zip"
	zip -r $foldername".zip" $foldername
fi

