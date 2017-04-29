import exceptions.InvalidAnalysisState;
import exceptions.InvalidStockSymbolException;
import exceptions.StockTickerConnectionError;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class StockQuoteAnalyzerTest {
    @Mock
    private StockQuoteGeneratorInterface generatorMock;
    @Mock
    private StockTickerAudioInterface audioMock;

    private StockQuoteAnalyzer analyzer;

    @BeforeMethod
    public void setUp() throws Exception {
        generatorMock = mock(StockQuoteGeneratorInterface.class);
        audioMock = mock(StockTickerAudioInterface.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        generatorMock = null;
        audioMock = null;
    }

    @Test(expectedExceptions = InvalidStockSymbolException.class)
    public void constructorShouldThrowExceptionWhenSymbolIsInvalid() throws Exception {
        analyzer = new StockQuoteAnalyzer("ZZZZZZZZZ", generatorMock, audioMock);
    }

    @Test (expectedExceptions = InvalidAnalysisState.class)
    public void getCurrentPriceShouldThrowExceptionWhenCurrentQuoteIsNull() throws Exception{
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.getCurrentPrice();
    }

    @Test
    public void getCurrentPriceShouldReturnCurrentPrice()throws Exception{
        double previousClose = 12.44;
        double lastTrade = 15.94;
        double change = 0.06;
        StockQuote mockQuote = new StockQuote("A",previousClose,lastTrade,change);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.refresh();
        double price = analyzer.getCurrentPrice();

        assertEquals(price, lastTrade);
    }

    @Test(expectedExceptions = InvalidAnalysisState.class)
    public void getChangeSinceCloseShouldShouldThrowExceptionWhenTheCurrentQuoteIsNull() throws Exception{
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.getChangeSinceClose();
    }

    @Test
    public void getChangeSinceCloseShouldReturnChange() throws Exception{
        double previousClose = 12.44;
        double lastTrade = 15.94;
        double change = 0.06;
        StockQuote mockQuote = new StockQuote("A",previousClose,lastTrade,change);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.refresh();
        double changeSinceClose = analyzer.getChangeSinceClose();

        assertEquals(change,changeSinceClose);
    }

    @Test
    public void getPercentChangeSinceCloseShouldReturnPercentChange() throws Exception{
        double previousClose = 12.44;
        double lastTrade = 15.94;
        double change = 0.06;
        StockQuote mockQuote = new StockQuote("A",previousClose,lastTrade,change);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        double percentChange = (change/previousClose) * 100;
        analyzer.refresh();
        double percentChangeSinceClose = analyzer.getPercentChangeSinceClose();


        assertEquals(percentChange,percentChangeSinceClose);
    }



    @Test(expectedExceptions = InvalidAnalysisState.class)
    public void getPercentChangeSinceCloseShouldShouldThrowExceptionWhenTheCurrentQuoteIsNull() throws Exception{
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.getPercentChangeSinceClose();
    }

    @Test
    public void changeSinceLastCheckShouldReturn0ifValueHasNotChanged()throws Exception{

        double previousClose = 12.44;
        double lastTrade = 15.94;
        double change = 0.06;
        StockQuote mockQuote = new StockQuote("A",previousClose,lastTrade,change);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.refresh();
        analyzer.refresh();
        double changeSinceLastCheck = analyzer.getChangeSinceLastCheck();


        assertEquals(0.0, changeSinceLastCheck);
    }

    @Test(expectedExceptions = InvalidAnalysisState.class)
    public void changeSinceLastCheckShouldThrowExceptionIfPreviousQuoteIsNull()throws Exception {

        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.refresh(); //this sets the quote the first time but previous quote should still be null
        analyzer.getChangeSinceLastCheck();

    }

    @Test
    public void changeSinceLastCheckShouldReturnChangeifValueHasChanged()throws Exception{

        double previousClose = 12.44;
        double lastTrade = 15.94;
        double change = 0.06;
        StockQuote mockQuote = new StockQuote("A",previousClose,lastTrade,change);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);
        analyzer = new StockQuoteAnalyzer("A", generatorMock, audioMock);

        analyzer.refresh();
        double newLastTrade = 16.94;
        mockQuote = new StockQuote("A",previousClose, lastTrade, change);
        analyzer.refresh();
        double changeSinceLastCheck = analyzer.getChangeSinceLastCheck();
        double actualChange = newLastTrade - lastTrade;

        assertEquals(actualChange, changeSinceLastCheck);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void constructorShouldThrowExceptionWhenStockQuoteSourceIsNull() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, null, audioMock);
    }

    @Test (expectedExceptions = StockTickerConnectionError.class)
    public void refreshShouldThrowExceptionWhenStockCannotConnect() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        doThrow(new StockTickerConnectionError()).when(generatorMock).getCurrentQuote();

        analyzer.refresh();
    }

    @Test
    public void playAppropriateAudioShouldPlayErrorMusicWhenPercentChangeThrowsError() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        analyzer.playAppropriateAudio();

        verify(audioMock, times(1)).playErrorMusic();
    }

    @Test
    public void playAppropriateAudioShouldPlayHappyMusicWhenPercentChangeIncreases() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", 1, 1, 1);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.refresh();
        analyzer.playAppropriateAudio();

        verify(audioMock, times(1)).playHappyMusic();
    }

    @Test
    public void playAppropriateAudioShouldPlaySadMusicWhenPercentChangeDecreases() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", 1, 1, -1);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.refresh();
        analyzer.playAppropriateAudio();

        verify(audioMock, times(1)).playSadMusic();
    }

    @Test
    public void playAppropriateAudioShouldDoNothingWhenPercentChangeIsZero() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", 1, 1, 0);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.refresh();
        analyzer.playAppropriateAudio();

        verify(audioMock, times(0)).playSadMusic();
        verify(audioMock, times(0)).playHappyMusic();
        verify(audioMock, times(0)).playErrorMusic();
    }

    @Test
    public void playAppropriateAudioShouldDoNothingWhenPercentChangeIsLessThanOne() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", 1, 1, 0.0099);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.refresh();
        analyzer.playAppropriateAudio();

        verify(audioMock, times(0)).playSadMusic();
        verify(audioMock, times(0)).playHappyMusic();
        verify(audioMock, times(0)).playErrorMusic();
    }

    @Test
    public void playAppropriateAudioShouldDoNothingWhenPercentChangeIsOne() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", 1, 1, 0.01);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.refresh();
        analyzer.playAppropriateAudio();

        verify(audioMock, times(0)).playSadMusic();
        verify(audioMock, times(0)).playHappyMusic();
        verify(audioMock, times(0)).playErrorMusic();
    }

    @Test
    public void playAppropriateAudioShouldPlaySadMusicWhenPercentChangeIsNegOne() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", (1/100), 1, -0.00001);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.refresh();
        analyzer.playAppropriateAudio();

        verify(audioMock, times(1)).playSadMusic();
    }

    @Test
    public void getSymbolShouldReturnSymbolWhenCalled() throws Exception {
        String testSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(testSymbol, generatorMock, audioMock);

        assertEquals(testSymbol, analyzer.getSymbol());
    }

    @Test
    public void getPreviousCloseShouldReturnPreviousCloseWhenValid() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", 1, 1, -1);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.refresh();
        analyzer.refresh();

        assertEquals(mockQuote.getClose(), analyzer.getPreviousClose());
    }

    @Test (expectedExceptions = InvalidAnalysisState.class)
    public void getPreviousCloseShouldThrowExceptionWhenInvalid() throws Exception {
        String validSymbol = "ZUMZ";
        analyzer = new StockQuoteAnalyzer(validSymbol, generatorMock, audioMock);

        StockQuote mockQuote = new StockQuote("A", 1, 1, -1);
        when(generatorMock.getCurrentQuote()).thenReturn(mockQuote);

        analyzer.getPreviousClose();
    }
}