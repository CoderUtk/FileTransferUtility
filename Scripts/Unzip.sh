#!/bin/bash
destination="/home/cloud_as1/users/utkarsh"
destinationZip="TXTs.zip"
cd $destination
unzip $destinationZip
rm $destinationZip
