package tmeval.data

import java.io._


/**
 * Extract the files from the Reuters archive. Original file obtained from:
 *
 *   http://kdd.ics.uci.edu/databases/reuters21578/reuters21578.html
 *
 * @author jasonbaldridge
 */
object ReutersPreparer {

  import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
  import scala.xml.parsing._

  import org.apache.commons.vfs2.{VFS,FileObject}
  import tmeval.Constants.TMEVAL_DIR

  lazy val parser = CorpusUtil.getNonValidatingXmlParser

  def main(args: Array[String]) {
    extract()
  }

  def extract() {
    println("Extracting Reuters data.")

    val tmevalDir = new File(TMEVAL_DIR)
    val outputDir = new File(tmevalDir, "data/extracted/reuters")
    outputDir.mkdirs

    val fileDescr = "tbz2:file:/" + tmevalDir.getAbsolutePath + "/data/orig/reuters21578.tar.bz2"
    val topDir = VFS.getManager.resolveFile(fileDescr).getChild("reuters21578")

    println("Processing files in " + topDir.getName + " and saving in " + outputDir)

    val train = new File(outputDir, "/train")
    train.mkdirs

    val eval = new File(outputDir, "/eval")
    eval.mkdirs

    val adapter = new NoBindingFactoryAdapter

    for (reutersFile <- topDir.getChildren if reutersFile.getName.getBaseName.endsWith("sgm")) {
      val name = reutersFile.getName.getBaseName
      println("** " + name)
      val number = name.slice(6,9)
      val directory = if (number.toInt < 17) train else eval
      val source = scala.xml.Source.fromInputStream(reutersFile.getContent.getInputStream)
      val fullSgml = adapter.loadXML(source, new SAXFactoryImpl().newSAXParser)

      for (articleSgml <- (fullSgml \ "REUTERS")) {
        val id = (articleSgml \ "@newid").text
        val text = (articleSgml \ "TEXT").text
        CorpusUtil.writeString(directory, number + "-" + id + ".txt", text)
      }
    }
  }

}
