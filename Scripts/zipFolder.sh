cd $HOME
source="/home/qc9/users/utkarsh"
cd $source
foldername=$(basename $source)
if [[ -d $source ]]; then
	echo "Is Folder"
	zip -r $foldername".zip" *
fi