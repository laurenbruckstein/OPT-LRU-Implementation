# cs1550 Project3 Writeup

## Data Comparisons

The following data was collected by running the "1.trace" tracefile. 

Between OPT and LRU, it is evident that OPT is much more efficient. LRU has a significantly greater number of page faults in each instance. LRU also has a greater number of writes to disk, when there is a difference.

Between 16 and 1024 frames, it seems there were a significantly larger number of page faults for both LRU and OPT when there are less frames. This makes sense becuase there are less pages that can fit into memory when there is a smaller number of frames. A pageFault occurs when a page was not found in memory, and needs to be inserted. There is also a larger number of writes to disk at 16 frames than with 1024 frames. Writes to disk occur when a page is evicted and it has its dirty bit set, meaning the page has been modifyed. It also makes sense for there to be more writes to disk with less frames, again because less pages will be capable of being stored in memory. It can be concluded that a larger number of frames, will lead to less pageFaults and writes to disk.

Looking at pageSizes, both OPT and LRU have significantly less pageFaults and writes to disk with a pageSize of 4 MB as compared to 4 KB. A larger pageSize may waste memory space and cause fragmentation. Potentially, portions of memory will go unused with too large of a pageSize, hence wasting memory. Considering that there is unused portions of memory, it makes sense that there will be less page Faults and writes to disk. Data wouldn't need to be stored on disk, since there is more than enough room in memory. You can see this in Figure 8, which has a large frame and page size. 




###	Graphs for 16 frames and a page size of 4 KB:

![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphPF1.png)
##### Figure 1.  



![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphWD1.png)
##### Figure 2. 



###	Graphs for 1024 frames and a page size of 4KB:

![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphPF2.png)
##### Figure 3.



![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphWD2.png)
##### Figure 4. 



###	Graphs for 16 frames and a page size of 4 MB:

![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphPF3.png)
##### Figure 5. 



![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphWD3.png)
##### Figure 6. 



###	Graphs for 1024 frames and a page size of 4 MB:

![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphPF4.png)
##### Figure 7. 



![](https://github.com/cs1550-2221/cs1550-project3-laurenbruckstein/blob/main/cs1550-graphWD4.png)
##### Figure 8. 

