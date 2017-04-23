#include<stdio.h>
#include<stdlib.h>
#include<pthread.h>
 
#define MAX_FACS (16)   // Maximum number of factors the program can  handle
#define BUFFER_SIZE (10)// Size of buffers
 
int args_index_in;      // Next free slot to insert a number to be factored
int args_index_out;     // Next usable slot of a number to be factored and be sent to the printing bufffer
int factors_index_in;   // Next free slot to point to array of factors in the printing buffer
int factors_index_out;  // Next usable slot to print the factors 
int* args_buffer;       // factoring buffer
int** factors_buffer;   // printing buffer
int g_argc;             // globalizing argc
int count_down;         // number of args to process
int wait;               // flag to make a buffer wait until more data is available
 
 
int* factoring_func(int n) //----------------------------------------------------------------------factoring_func
{
    int* factors = malloc(sizeof(int) * MAX_FACS); // factor array created for all factors
    int i = 2;
    int k = 0;
    factors[k++] = n;
    while(n > 1) //prime factor algorithm
    {
        while((n%i) == 0)
        {
            factors[k] = i;
            k++;
            n /= i;
        }
        i += 1;
    }
    k++;
    factors[k] = '\0';
    return factors;
}
 
 
void *producer_func(void *p)  //----------------------------------------------------------------------producer_func
{
    pthread_mutex_t *pmutex = (pthread_mutex_t *)p;
 
    while(count_down != 1 || args_buffer[args_index_out] != 0)
    {
        while(factors_buffer[factors_index_in] != NULL || args_buffer[args_index_out] == 0);
 
        pthread_mutex_lock(pmutex);                                                         //Critical section started
        factors_buffer[factors_index_in++] = factoring_func(args_buffer[args_index_out]);   // top of buffer1 factored into buffer2
        args_buffer[args_index_out++] = 0;
        factors_index_in = factors_index_in % BUFFER_SIZE;
        args_index_out = args_index_out % BUFFER_SIZE;
        pthread_mutex_unlock(pmutex);                                                       // Critical section ended
 
    }
    count_down--;
    wait--;
    return NULL;
}
 
 
void *consumer_func(void *p)  //----------------------------------------------------------------------consumer_func
{
    pthread_mutex_t *pmutex = (pthread_mutex_t *)p;
    int* factor_array;
 
    while(factors_buffer[factors_index_out]!= NULL || count_down != 0)
    {
        while(factors_buffer[factors_index_out] == NULL);       // wait for a factorization to process
         
        pthread_mutex_lock(pmutex);                             // Critical Section started
        factor_array = factors_buffer[factors_index_out];
        factors_buffer[factors_index_out++] = NULL;
        factors_index_out = factors_index_out % BUFFER_SIZE; 
        pthread_mutex_unlock(pmutex);                           // Critical section ended
         
        int k = 0;
        printf("%d: ", factor_array[k++]);
         
        while(factor_array[k] != '\0')
        {
            printf("%d ", factor_array[k++]);
        }
        printf("\n");
    }
    wait--;
    return NULL;
}
 
int main(int argc, char* argv[])  //----------------------------------------------------------------------main
{
    args_buffer = malloc(sizeof(int) * BUFFER_SIZE);        // global array for arguments
    factors_buffer = malloc(sizeof(int*) * BUFFER_SIZE);    // global array for factors
    g_argc = argc - 1;                                      // argc globalized
    args_index_in = 0;
    args_index_out= 0;                                              
    factors_index_in = 0;
    factors_index_out = 0;                                              
    count_down = argc;
    wait = 2;
 
    int i;
    for(i = 0; i < BUFFER_SIZE; i++)
    {
        args_buffer[i] = 0;
        factors_buffer[i] = NULL;
    }
 
    pthread_t producer_thread;                              //producer thread initialized
    pthread_t consumer_thread;                              //consumer thread initialized
    pthread_mutex_t my_mutex = PTHREAD_MUTEX_INITIALIZER;   //mutex created
    pthread_create(&producer_thread, NULL, producer_func, &my_mutex);   //producer started
    pthread_create(&consumer_thread, NULL, consumer_func, &my_mutex);   //consumer started
 
    for(i = 1; i < argc; i++)
    {
        while(args_buffer[args_index_in] != 0);             //wait for for args buffer to have an empty slot
         
        pthread_mutex_lock(&my_mutex);                      // Critical Section started
        args_buffer[args_index_in++] = atoi(argv[i]);       // global int argument array filling
        args_index_in = args_index_in % BUFFER_SIZE;            // if going OOB then set to beginning
        pthread_mutex_unlock(&my_mutex);                    // Critical Section ended
 
        count_down--;                                       // decrement amount of args remaining
    }                           
 
    while(wait);                                            //wait for threads to complete 
    pthread_join(producer_thread, NULL);                    //producer ended
    pthread_join(consumer_thread, NULL);                    //consumer ender
     
    return 0;
}
