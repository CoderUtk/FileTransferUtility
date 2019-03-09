cd $HOME
source=""
if [[ -d $source ]]; then
	echo "Is Folder"
	zip -r $source".zip" $source
fi