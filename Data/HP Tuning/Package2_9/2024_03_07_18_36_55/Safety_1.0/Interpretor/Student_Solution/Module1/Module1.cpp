#include <iostream>
#include <fstream>
#include <string>

using namespace std;

int readConstant(string path){
    fstream newfile;
    newfile.open(path+"./Student_Solution/Module1/config.txt");
    int res = 0;
    if(newfile.is_open()){
        string tp;
        getline(newfile, tp);
        res = stoi(tp);
        newfile.close();
    }
    return res;
}

class Number{
    public:
        virtual void print() = 0;
};

class GreaterThan: public Number{
    public:
        void print(){
            cout << "GreaterThan" << endl;
        }
};

class LessThan: public Number{
    public:
        void print(){
            cout << "LessThan" << endl;
        } 
};

int main(int argc, char *argv[]){
    int n = readConstant(argv[2]);
    int v = stoi(argv[1]);
    Number* obj;
    if(n > v){
        obj = new GreaterThan();
    }
    if(n < v){
        obj = new LessThan();
    }
    obj->print();
    delete obj;
    return 0;
}