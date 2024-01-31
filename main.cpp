#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <string>

using namespace std;

string triangleType(int s1, int s2, int s3){
    
}

int main()
{
    ifstream file("input.txt");
    string strV1;
    string strV2;
    string strV3;
    getline(file, strV1);
    getline(file, strV2);
    getline(file, strV3);
    vector<int> sides;
    sides.push_back(stoi(strV1));
    sides.push_back(stoi(strV2));
    sides.push_back(stoi(strV3));
    sort(sides.begin(), sides.end());
    for (auto x : sides) 
        cout << x << " "; 
    file.close();
}