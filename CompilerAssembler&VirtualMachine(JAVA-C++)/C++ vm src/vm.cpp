#include <iostream> 
#include <fstream> 
#include <regex> 
#include <map> 
#include <string> 

struct instruction {
	int opcode;
	int operand1;
	int operand2;
} ins;


int sl = 100000; // stack limit
int sp;			 //top of stack
int fp;			 //bottom of current frame
int sb = 199996; //stack base
int fpf;

const int MEM_SIZE = 200000;
const int REG_SIZE = 9;
char mem[MEM_SIZE];						// staticdata:0 Code:100 Heap:6000 Stack:7000
int reg[REG_SIZE];						// registers
int zero = 0;							// trap (operand2) filler
bool running = 1;						// exit flag
int pc = 1000;							//program counter 100 is code segment beginning
int codeIndex = 1000;					// pass2 index for 4 byte insertion or operator and operands
char IR[12];							// IR is the pointer to the 12 byte intructions
std::string myLine;						// temp getline value
std::map<std::string, int> LabelsMap;
std::map<std::string, int> operatorMap;

void initOperatorMap() {
	operatorMap["TRP"] = 0;
	operatorMap["JMP"] = 1;
	operatorMap["JMR"] = 2;
	operatorMap["BNZ"] = 3;
	operatorMap["BGT"] = 4;
	operatorMap["BLT"] = 5;
	operatorMap["BRZ"] = 6;
	operatorMap["MOV"] = 7;
	operatorMap["LDA"] = 8;
	operatorMap["STR"] = 9;
	operatorMap["LDR"] = 10;
	operatorMap["STB"] = 11;
	operatorMap["LDB"] = 12;
	operatorMap["ADD"] = 13;
	operatorMap["ADI"] = 14;
	operatorMap["SUB"] = 15;
	operatorMap["MUL"] = 16;
	operatorMap["DIV"] = 17;
	operatorMap["AND"] = 18;
	operatorMap["OR"] = 19;
	operatorMap["CMP"] = 20;
}

void pass1(std::string fileName) {
	std::ifstream myfile(fileName);
	if (myfile.is_open())
	{
		int staticDataIndex = 0;
		while (getline(myfile, myLine))
		{
			if (myLine[0] != '/') { // not a comment
				std::string myTokens[4];
				std::regex myRegex("[ ]");
				std::copy(std::sregex_token_iterator(myLine.begin(), myLine.end(), myRegex, -1),
					std::sregex_token_iterator(),
					myTokens);
				if (myTokens[0][0] == '[') {
					if (myTokens[1] == ".INT") {
						LabelsMap[myTokens[0]] = staticDataIndex;
						staticDataIndex += 4;
					}
					else if (myTokens[1] == ".BYT") {
						LabelsMap[myTokens[0]] = staticDataIndex;
						staticDataIndex += 1;
					}
					else {
						LabelsMap[myTokens[0]] = pc;
						staticDataIndex += 4;
						pc += 12;
					}
				}
				else if (myTokens[0] == ".INT") {
					staticDataIndex += 4;
				}
				else if (myTokens[0] == ".BYT") {
					staticDataIndex += 1;
				}
				else {
					pc += 12;
				}
			}
		}
		myfile.close();
		pc = 1000;
	}

	else std::cout << "Unable to open file";
}

void pass2(std::string fileName) {
	std::ifstream myfile(fileName);
	int staticDataIndex = 0;
	initOperatorMap();

	if (myfile.is_open()) {
		int test = 1;
		while (getline(myfile, myLine)) {
			if (myLine[0] != '/') { //comment
				std::string myTokens[4];
				int tempOp1;
				int tempOp2;
				std::regex myRegex("[ ]");
				std::copy(std::sregex_token_iterator(myLine.begin(), myLine.end(), myRegex, -1),
					std::sregex_token_iterator(),
					myTokens);
				if (myTokens[0][0] == '[') { // hasLabel
					if (myTokens[1] == ".INT") { // is int
						tempOp2 = stoi(myTokens[2]);
						memcpy(&mem[staticDataIndex], &tempOp2, sizeof(int));
						staticDataIndex += 4;
					}
					else if (myTokens[1] == ".BYT") { // is byte
						if(myTokens[2][1]  == '\\'){
							char c;
							if (myTokens[2][2] == 'n') {
								c = 0xA;
							}
							else if (myTokens[2][2] == 't') {
								c = 0x9;
							}
							else if (myTokens[2][2] == 'r') {
								c = 0xd;
							}
							else if (myTokens[2][2] == 's') {
								c = 0x20;
							}
							memcpy(&mem[staticDataIndex], &c, sizeof(char));
						}
						else {
							memcpy(&mem[staticDataIndex], &myTokens[2][1], sizeof(char));
						}
						staticDataIndex += 1;
					}
					else { // is branch
						memcpy(&mem[staticDataIndex], &codeIndex, sizeof(int)); // make label for branch
						staticDataIndex += 4;
						tempOp1 = operatorMap[myTokens[1]];
						if (myTokens[3][0] == '(') { tempOp1 += 12; }
						memcpy(&mem[codeIndex], &tempOp1, sizeof(int)); // insert opcode
						codeIndex += 4;
						if (tempOp1 == 0) { //trp
							tempOp2 = stoi(myTokens[2]);
							memcpy(&mem[codeIndex], &tempOp2, sizeof(int));
							codeIndex += 4;
							memcpy(&mem[codeIndex], &zero, sizeof(int));
							codeIndex += 4;
						}
						else {
							int tempInt1 = (myTokens[2][1]) - '0';
							memcpy(&mem[codeIndex], &tempInt1, sizeof(int)); // insert reg num in operand1
							codeIndex += 4;
							if (myTokens[2][0] = 'R' && myTokens[3][0] == 'R') { // 2 regs
								tempInt1 = (myTokens[3][1]) - '0';
								memcpy(&mem[codeIndex], &tempInt1, sizeof(int));
								codeIndex += 4;
							}
							else if (myTokens[3][0] == '[') { // direct
								memcpy(&mem[codeIndex], &(LabelsMap[myTokens[3]]), sizeof(int));
								codeIndex += 4;
							}
							else if (myTokens[3][0] == '(') { // indirect
								tempOp1 = atoi(&myTokens[3][2]);
								memcpy(&mem[codeIndex], &tempOp1, sizeof(int*));
								codeIndex += 4;
							}
							else { // immidiate
								tempOp1 = stoi(myTokens[3]);
								memcpy(&mem[codeIndex], &tempOp1, sizeof(int));
								codeIndex += 4;
							}
						}
					}
				} // end of hasLabels

				else { // no label
					if (myTokens[0] == ".BYT") { // is byte
						memcpy(&mem[staticDataIndex], &myTokens[1][1], sizeof(char));
						staticDataIndex += 1;
					}
					else if (myTokens[0] == ".INT") { // is int
						tempOp2 = stoi(myTokens[1]);
						memcpy(&mem[staticDataIndex], &tempOp2, sizeof(int));
						staticDataIndex += 4;
					}
					else {
						tempOp1 = operatorMap[myTokens[0]]; // switch loads and strs to indirect
						if (myTokens[2][0] == '(') {
							tempOp1 += 12;
						}
						memcpy(&mem[codeIndex], &tempOp1, sizeof(int)); // insert binary operator
						codeIndex += 4;
						if (operatorMap[myTokens[0]] == 0) { //is trp 
							tempOp1 = stoi(myTokens[1]);
							memcpy(&mem[codeIndex], &tempOp1, sizeof(int));
							codeIndex += 4;
							memcpy(&mem[codeIndex], &zero, sizeof(int));
							codeIndex += 4;
						}
						else if (myTokens[1][0] == '[') {
							memcpy(&mem[codeIndex], &(LabelsMap[myTokens[1]]), sizeof(int));
							codeIndex += 4;
							memcpy(&mem[codeIndex], &zero, sizeof(int));
							codeIndex += 4;
						}
						else {
							tempOp1 = atoi(&myTokens[1][1]);
							memcpy(&mem[codeIndex], &tempOp1, sizeof(int));
							codeIndex += 4;
							if (myTokens[1][0] = 'R' && myTokens[2][0] == 'R') { // 2 regs
								tempOp1 = (myTokens[2][1]) - '0';
								memcpy(&mem[codeIndex], &tempOp1, sizeof(int));
								codeIndex += 4;
							}
							else if (myTokens[2][0] == '[') { // mem dir
								memcpy(&mem[codeIndex], &(LabelsMap[myTokens[2]]), sizeof(int));
								codeIndex += 4;
							}
							else if (myTokens[2][0] == '(') { // mem indir
								tempOp1 = atoi(&myTokens[2][2]);
								memcpy(&mem[codeIndex], &tempOp1, sizeof(int*));
								codeIndex += 4;
							}
							else { // immediate
								tempOp1 = stoi(myTokens[2]);
								memcpy(&mem[codeIndex], &tempOp1, sizeof(int));
								codeIndex += 4;
							}
						} //end of not label
					}
				}
			}
		}
		myfile.close();
	}
	else {
		std::cout << "Unable to open file";
	}
}

void fetch() {
	memcpy(IR, &mem[pc], sizeof(char) * 12);
}

void decode() {
	memcpy(&ins.opcode,	  &IR[0], sizeof(int));
	memcpy(&ins.operand1, &IR[4], sizeof(int));
	memcpy(&ins.operand2, &IR[8], sizeof(int));
}

void execute() {
	int input;
	switch (ins.opcode) {
	case 0:/* trp */
		switch (ins.operand1) {
		case 0:
			running = 0;
			break;
		case 1:
			std::cout << *((int*)(&(reg[3])));
			break;
		case 2:
			std::cin >> input;
			reg[3] = input;
			while (getchar() != '\n');
			break;
		case 3:
			std::cout << (char)reg[3];
			break;
		case 4:
			reg[3] = getchar();
			while (getchar() != '\n');
			break;
		}
		pc += 12;
		break;
	case 1: /* jmp */
		pc = ins.operand1;
		break;
	case 2: /* jmr */
		pc = reg[ins.operand1];
		break;
	case 3: /* bnz */
		if (reg[ins.operand1] != 0)
			pc = ins.operand2;
		else {
			pc += 12;
		}
		break;
	case 4: /* bgt */
		if (reg[ins.operand1] > 0)
			pc = ins.operand2;
		break;
	case 5: /* blt */
		if (reg[ins.operand1] < 0)
			pc = ins.operand2;
		else {
			pc += 12;
		}
		break;
	case 6: /* brz */
		if (reg[ins.operand1] == 0)
			pc = ins.operand2;
		else {
			pc += 12;
		}
		break;
	case 7: /* mov */
		reg[ins.operand1] = reg[ins.operand2];
		pc += 12;
		break;
	case 8: /* lda */
		reg[ins.operand1] = ins.operand2;
		pc += 12;
		break;
	case 9: /* str int*/
		*((int*)(&(mem[ins.operand2]))) = reg[ins.operand1];
		pc += 12;
		break;
	case 10: /* ldr int*/
		reg[ins.operand1] = *((int*)(&(mem[ins.operand2])));
		pc += 12;
		break;
	case 11: /* stb byte*/
		mem[ins.operand2] = reg[ins.operand1];
		pc += 12;
		break;
	case 12: /* ldb byte*/
		reg[ins.operand1] = mem[ins.operand2];
		pc += 12;
		break;
	case 13: /* add */
		reg[ins.operand1] += reg[ins.operand2];
		pc += 12;
		break;
	case 14: /* adi */
		reg[ins.operand1] += ins.operand2;
		pc += 12;
		break;
	case 15: /* sub */
		reg[ins.operand1] -= reg[ins.operand2];
		pc += 12;
		break;
	case 16: /* mul */
		reg[ins.operand1] *= reg[ins.operand2];
		pc += 12;
		break;
	case 17: /* div */
		reg[ins.operand1] /= reg[ins.operand2];
		pc += 12;
		break;
	case 18: /* and */
		reg[ins.operand1] = (reg[ins.operand1] & reg[ins.operand2]);
		pc += 12;
		break;
	case 19: /* or */
		if (reg[ins.operand1] < 0 || reg[ins.operand1] > 0 || reg[ins.operand2] < 0 || reg[ins.operand2] > 0)
			reg[ins.operand1] = 1;
		else 
			reg[ins.operand1] = 0;
		pc += 12;
		break;
	case 20: /* cmp */
		if (reg[ins.operand1] == reg[ins.operand2])
			reg[ins.operand1] = 0;
		else if (reg[ins.operand1] > reg[ins.operand2])
			reg[ins.operand1] = 1;
		else
			reg[ins.operand1] = -1;
		pc += 12;
		break;
	case 21: /*str*/
		*((int*)(&(mem[reg[ins.operand2]]))) = reg[ins.operand1];
		pc += 12;
		break;
	case 22: /*ldr*/
		reg[ins.operand1] = *((int*)(&(mem[reg[ins.operand2]])));
		pc += 12;
		break;
	case 23: /*stb*/
		mem[reg[ins.operand2]] = reg[ins.operand1];
		pc += 12;
		break;
	case 24: /*ldb*/
		reg[ins.operand1] = mem[reg[ins.operand2]];
		pc += 12;
		break;
	}
}

void main(int argc, char* argv[]) {
	if (argc != 2) {
		std::cout << "This executable requires one parameter: asm file";
		exit(1);
	}
	std::string fileName = (std::string)argv[1];
	pass1(fileName);
	pass2(fileName);
	while (running) {
		fetch();
		decode();
		execute();
	}
}