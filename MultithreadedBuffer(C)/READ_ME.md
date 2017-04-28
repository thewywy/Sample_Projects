# MultiThreaded Prime Factorial Solver by Wyatt Sorenson

Requirements: gcc

How to use: 

$ ./ThreadedBuffer 4
4: 2 2

$ ./ThreadedBuffer 65356  
65356: 2 2 16339  

$ ./ThreadedBuffer 65536  
65536: 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2  

$ ./ThreadedBuffer 157934563  
157934563: 59 113 23689  

$ ./ThreadedBuffer 2 3 5 64 257 17 3333 3428567 34781 1  
2: 2  
3: 3  
5: 5  
64: 2 2 2 2 2 2  
257: 257  
17: 17  
3333: 3 11 101  
3428567: 3428567  
34781: 34781  
1:  

thewywy:~/workspace $ ./ThreadedBuffer 22 222 2222 22222 222222 7777 4 33333 99999999 555  
22: 2 11  
222: 2 3 37  
2222: 2 11 101  
22222: 2 41 271  
222222: 2 3 7 11 13 37  
7777: 7 11 101  
4: 2 2  
33333: 3 41 271  
99999999: 3 3 11 73 101 137  
555: 3 5 37  

This program takes up to 16 positive integer values and processes them within 3 threads staging them in 
producer and consumer buffers to derive their prime factorizations. 

Output: prints a listing of each argument's prime factorization