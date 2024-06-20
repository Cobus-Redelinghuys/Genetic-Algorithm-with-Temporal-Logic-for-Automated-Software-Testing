#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <string>

using namespace std;

#define equilateral "equilateral"
#define isosceles "isosceles"
#define scalene "scalene"
#define not_a_triangle "not a triangle"

string triangleType(int a, int b, int c)
{
    string type;
    if (a + b <= c)
    {
        type = not_a_triangle;
    }
    else
    {
        type = scalene;
        if (a == b && b == c)
        {
            type = equilateral;
        }
        else if (a == b || b == c)
        {
            type = isosceles;
        }
    }
    return type;
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
    file.close();
    cout << triangleType(sides[0], sides[1], sides[2]) << endl;
}