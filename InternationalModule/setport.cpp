// portsetter by Wyatt Sorenson

#include <iostream>
#include <string>
#include <cstring>
#include <stdlib.h>
#include <fstream>
using namespace std;

void usage(string lang);
string getMsg(int index, string lang);

int main(int argc, char* args[]) {
	const string P = "-p";
	const string PORT = "--port";
	const string QM = "-?";
	const string H = "-h";
	const string HELP = "--help";
	const string EM = "-!";
	const string ABOUT = "--about";
	const string V = "-v";
	const string VERSION = "--version";
	const string E = "-e";
	const string ENV = "PORT";
	string port = "8080";
	string lang;
	string la = (string)getenv("LANG");
	string file;
	
	if(la == "C" || la == "en") {
		lang = "en";
	}
	else if (la == "es") {
		lang = "es";
	}
	else{
		lang = "en";
	}
	
	const char* tempPath = ("setport.about_" + lang + ".txt").c_str();
	std::ifstream infile;
	infile.open(tempPath);
	
	if (!infile.good()) {
		cout << "Missing translation files. Using English." << endl;
		lang = "en";
	}
	
	infile.close();

	//no args prints usage
	if (argc == 1) {
		usage(lang);
		return 0;
	}
	
	
	string args1 = args[1];
	string args2 = "";
	
	//print About
	if (args1 == EM || args1 == ABOUT) {
		if (argc > 2) {
			cout << getMsg(1, lang) << endl;
			usage(lang);
			return 5;
		}
		else {
			ifstream aboutIn;
			file = "setport.about_" + lang + ".txt";
			aboutIn.open(file.c_str());
			string line;
			getline(aboutIn, line);
			cout << line << endl;
			aboutIn.close();
			return 0;
		}
	}
	
	//print Version
	if (args1 == V || args1 == VERSION) {
		if (argc > 2) {
			cout << getMsg(2, lang) << endl;
			usage(lang);
			return 5;
		}
		else {
			cout << getMsg(3, lang) + "0.3" << endl;
			return 0;
		}
	}
	
	//print help
	if (args1 == QM || args1 == H || args1 == HELP) {
		if (argc == 2) {
			usage(lang);
			return 0;
		}
		else {
			cout << getMsg(4, lang) << endl;
			usage(lang);
			return 4;
		}

	}

	//print port listening
	if (args1 == P || args1 == PORT) {
		if (argc == 2) {
		    cout << getMsg(5, lang) << endl;
		    usage(lang);
		    return 3;
		}
		args2 = args[2];
		if (args2 == E) {
			if (argc == 3) {
				cout << getMsg(10, lang) << port << endl;
				return 0;
			}
			if (argc == 4 && args[3] == ENV) {
				port = getenv(args[3]);
				cout << getMsg(10, lang) << port << endl;
				return 0;
			}
			if (argc < 4) {
				cout << getMsg(6, lang) << endl;
				usage(lang);
				return 6;
			}
		}
		if (argc > 3) {
		    cout << getMsg(7, lang) << endl;
		    usage(lang);
		    return 4;
		}
		else {
			if (atoi(args[2]) > 0 && atoi(args[2]) < 65536) {
				cout << getMsg(10, lang) << args2 << endl;;
				return 0;
			}
			else {
				cout << getMsg(8, lang) << args2 << endl;
				usage(lang);
				return 2;
			}
		}
	}
	
	cout << getMsg(9, lang) << endl;
	usage(lang);
	return 1;
}

//function usage: Explains how to use program
void usage(string lang) {
	ifstream aboutIn;
	string file = "setport.usage_" + lang + ".txt";
	aboutIn.open(file.c_str());
	string line;
    while ( getline (aboutIn, line) )
    {
      cout << line << endl;
    }
    aboutIn.close();
}

string getMsg(int index, string lang) {
	ifstream aboutIn;
	string file = "setport.msg_" + lang + ".txt";
	aboutIn.open(file.c_str());
	string line;
	for (int i = 0; i < index; i++) {
    	getline (aboutIn, line);
	}
    aboutIn.close();
    return line;
}