# Lab 7 Writeup
### Nathan Goihl, goihlnm@msoe.edu
### Luke Fliss, flisslj@msoe.edu
### SE 2832 031


## Bug Table
Original Line Number | Fault Description | Fix Description 
-------------------- | ----------------- | ---------------
119 | Happy music played for all positive values instead of only above 1 | ... |
151 | Throws exception unexpectedly | ... |
154 | Gets the close of the current quote instead of the previous | ... |
l85 | getChangeSinceClose throws a null pointer exception instead of a InvalidStateException| Changed the exception thrown to an invalidStateException |
187 | getChangeSinceClose returns an incorrect value | removed -currentQuote.getClose |
204 | getPercentChangeSinceClose returns 10X the value its supposed to | changed *100000 to *10000 |
220 | getChangeSinceLastCheck will always return 0.0 because its subtracting the same value from itself| changed -currentQuote.getLastTrade() to -previousQuote.getLastTrade|
220 | getChangeSinceLastCheck doesn't throw an invalid state exeption if there is no previous quote | added an invalidAnalysisState that is thrown when previousQuote is null |


## Final code coverage:
![Code Coverage](Link here)