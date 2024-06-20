#include <iostream>
#include <fstream>
#include <string>

using namespace std;

template<class T, class U>
struct Pair{
    T d1;
    U d2;
};

Pair<int,int> readConstant(string path){
    fstream newfile;
    newfile.open(path+"/Instructor_Solution/Module3/config.txt");
    int res1 = 0;
    int res2 = 0;
    if(newfile.is_open()){
        string tp;
        getline(newfile, tp);
        res1 = stoi(tp);
        getline(newfile, tp);
        res2 = stoi(tp);
        newfile.close();
    }
    Pair<int, int> result;
    result.d1 = res1;
    result.d2 = res2;
    return result;
}

class Position{
    public:
        virtual void print() = 0;
};

class Inbound: public Position{
    public:
        void print(){
            cout << "Inbetween boundaries" << endl;
        }
};

int main(int argc, char *argv[]){
    string path = argv[2];
    Pair<int, int> res = readConstant(path);
    int v = stoi(argv[1]);
    Position* obj;
    if(v > res.d1 && v < res.d2){
        obj = new Inbound();
    }
    obj->print();
    delete obj;
    return 0;
}