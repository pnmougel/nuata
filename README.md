This is your new Play application
=================================

# INSTALL

## Ubuntu 14.04 64bits

Add the repository and update the database

"""
sudo add-apt-repository ppa:openjdk-r/ppa
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
sudo apt-get update
"""

Install the required package

"""
sudo apt-get install git openjdk-8-jdk sbt 
"""

Change jdk version to 8
"""
sudo update-alternatives --config java
"""

Download the source
"""
git clone https://github.com/pnmougel/nuata.git
"""



This file will be packaged with your application, when using `activator dist`.
