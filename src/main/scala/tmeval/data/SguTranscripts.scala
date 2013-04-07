package tmeval.data

import java.io._

/**
 * Given a directory containing raw text of SGU episodes (one episode per file),
 * pull out the different segments into their own files, and put all episodes
 * before episode 360 into a train directory and all following ones into an eval
 * directory.
 *
 * @author jasonbaldridge
 */
object SguTranscriptPreparer {

  import org.apache.commons.vfs2.{VFS,FileObject}
  import tmeval.Constants.TMEVAL_DIR

  lazy val FileIdRE = """^.*_(\d+)$""".r

  def main(args: Array[String]) {
    extract()
  }

  def extract() {

    println("Extracting SGU Transcripts data.")

    val tmevalDir = new File(TMEVAL_DIR)
    val outputDir = new File(tmevalDir, "data/extracted/sgu")
    outputDir.mkdirs

    val fileDescr = "tbz2:file:/" + tmevalDir.getAbsolutePath + "/data/orig/sgu-2013-04-04.tar.bz2"
    val topDir = VFS.getManager.resolveFile(fileDescr).getChild("sgu")

    println("Processing files in " + topDir.getName + " and saving in " + outputDir)

    for (transcriptFile <- topDir.getChildren if !transcriptFile.getName.getBaseName.endsWith("md") ) {

      val name = transcriptFile.getName.getBaseName
      println("------- "  + name)

      val FileIdRE(id) = name
      val trainOrEvalDirName = if (id.toInt < 360) "train" else "eval"
      val trainOrEvalDir = new File(outputDir, trainOrEvalDirName)
      trainOrEvalDir.mkdirs

      val transcript = scala.xml.XML.load(transcriptFile.getContent.getInputStream)
      val body = (transcript \\ "div").filter(d=> (d \ "@id").text == "mw-content-text").head

      var sectionCounter = 0
      val utterances = (body \ "_").flatMap { elem => {
        val text = elem match {
          case <h2>{_*}</h2> => sectionCounter += 1; (elem \ "span")(1).text.trim
          case <p>{_*}</p> => elem.text.trim
          case _ => ""
        }
        if (text == "") None else Some(sectionCounter, text)
      }}

      val sections = 
        utterances
          .groupBy(_._1)
          .mapValues(_.unzip._2)
          .values
          .filter(_.length > 1)

      for (section <- sections) {
        val title :: rest = section
        val cleanedTitle = title.replaceAll("""[^A-Za-z]+""","_")
        val sectionFile = new File(trainOrEvalDir, name + "_" + cleanedTitle + ".txt")
        val writer = new PrintWriter(new BufferedWriter(new FileWriter(sectionFile)))
        rest.foreach(utterance => writer.write(utterance + "\n"))
        writer.close
      }
    }
  }

}


/**
 * Scrapes the SGU transcripts site to get episodes. This isn't needed to replicate
 * anything in the paper, but is useful if you want to run models on SGU transcripts
 * after more have been completed.
 *
 * You can specify the highest show id to pull on the command line (default=410).
 *
 * @author jasonbaldridge
 */
object SguTranscriptPuller {

  def main(args: Array[String]) {

    val maxEpisodeId = if (args.length > 0) args(0).toInt else 410

    val rawSgu = new File("data/sgu/raw")
    rawSgu.mkdirs

    println("Writing episodes to " + rawSgu)

    val urlPrefix = "http://www.sgutranscripts.org/wiki/"
    println("Downloading transcripts from " + urlPrefix)

    val filePrefix = "SGU_Episode_"

    for (showNumber <- 1 to maxEpisodeId) {
      val show = filePrefix+showNumber
      println("** " + show)
      val url = urlPrefix+show
      try {
        val localFile = new File(rawSgu, show)
        val showSource = io.Source.fromURL(url)
        val writer = new PrintWriter(new BufferedWriter(new FileWriter(localFile)))
        showSource.getLines.foreach(l=>writer.write(l+"\n"))
        writer.close
      } catch { 
        case e: Throwable => println("Unable to get episode: " + url)
      }
      Thread.sleep(1000) // Be nice to the site and wait a bit for the next request.
    }

  }

}

