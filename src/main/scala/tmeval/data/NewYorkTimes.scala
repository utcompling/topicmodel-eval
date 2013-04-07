package tmeval.data

import java.io._
import java.util.zip._


/**
 * Processes the New York Times portion of the English Gigaword corpus. This corpus
 * does not come with this code as you must have a license for it (and it is quite
 * large). You can obtain it from:
 *
 *   http://www.ldc.upenn.edu/Catalog/catalogEntry.jsp?catalogId=LDC2003T05
 *
 * @author jasonbaldridge
 */
object NytPreparer {

  import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
  import scala.xml.parsing._
  import tmeval.Constants.TMEVAL_DIR

  def main(args: Array[String]) {
    if (args.length == 0)
      println("You must specify the location of the 'nyt' directory of your"
              + " copy of the Gigaword corpus.")
    else 
      extract(args(0))
  }

  def extract(nytGigawordLocation: String) {

    println("Extracting New York Times data. (This will take a while.)")
    
    val raw = new File(nytGigawordLocation)

    val tmevalDir = new File(TMEVAL_DIR)
    val outputDir = new File(tmevalDir, "data/extracted/nyt")
    outputDir.mkdirs

    val train = new File(outputDir, "/train")
    train.mkdirs

    val eval = new File(outputDir, "/eval")
    eval.mkdirs

    println("Processing files in " + raw)

    val adapter = new NoBindingFactoryAdapter

    for (file <- raw.listFiles if file.getName.endsWith(".gz")) {
      val name = file.getName
      val number = name.slice(3,9)
      
      val directory = if (number.toInt < 200206) train else eval
      val source = scala.xml.Source.fromInputStream(new GZIPInputStream(new FileInputStream(file)))
      val fullSgml = adapter.loadXML(source, new SAXFactoryImpl().newSAXParser)
      for (articleSgml <- (fullSgml \ "DOC")) {
        val id = (articleSgml \ "@id").text
        val text = (articleSgml \ "TEXT").text
        CorpusUtil.writeString(directory, id, text)
      }
    }
  }

}
