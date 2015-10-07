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
sudo apt-get install git openjdk-8-jdk sbt postgresql-9.3
"""

Change jdk version to 8
"""
sudo update-alternatives --config java
"""

Download the source
"""
git clone https://github.com/pnmougel/nuata.git
"""

Configure the database 

"""
create_user <DB_USER_NAME> -P -l
sudo -i -u postgres
createdb <DB_NAME>
"""

Create and update application.conf with the database connexion information
* db.default.url
* db.default.username
* db.default.password

"""
cp conf/application.conf.default conf/application.conf
"""

To test the connection
"""
psql -U <DB_USER_NAME> -d <DB_NAME> -h 127.0.0.1 -W
"""
