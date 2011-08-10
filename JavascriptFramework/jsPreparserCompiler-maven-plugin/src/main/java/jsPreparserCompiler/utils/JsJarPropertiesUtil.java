package jsPreparserCompiler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.archiver.PomPropertiesUtil;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.IOUtil;

public class JsJarPropertiesUtil extends PomPropertiesUtil {
	
	private static final String GENERATED_BY_MAVEN = "Generated by Maven";

    private boolean sameContents( Properties props, File file )
            throws IOException
    {
        if ( !file.isFile() )
        {
            return false;
        }
        Properties fileProps = new Properties();
        InputStream istream = null;
        try 
        {
            istream = new FileInputStream( file );
            fileProps.load( istream );
            istream.close();
            istream = null;
            return fileProps.equals( props );
        }
        catch ( IOException e )
        {
            return false;
        }
        finally
        {
            IOUtil.close( istream );
        }
    }

    private void createPropertyFile( Properties properties, File outputFile,
                                     boolean forceCreation )
        throws IOException
    {
        File outputDir = outputFile.getParentFile();
        if ( outputDir != null  &&  !outputDir.isDirectory()  &&  !outputDir.mkdirs() )
        {
            throw new IOException( "Failed to create directory: " + outputDir );
        }
        if ( !forceCreation  &&  sameContents( properties, outputFile ) )
        {
            return;
        }
        OutputStream os = new FileOutputStream( outputFile );
        try
        {
            properties.store( os, GENERATED_BY_MAVEN );
            os.close(); // stream is flushed but not closed by Properties.store()
            os = null;
        }
        finally
        {
            IOUtil.close( os );
        }
    }

	/**
     * Creates the pom.properties file.
     */
    public void createPomProperties( MavenProject project, Archiver archiver, File pomPropertiesFile,
                                     boolean forceCreation, String debugFilename )
        throws ArchiverException, IOException
    {
        final String artifactId = project.getArtifactId();
        final String groupId = project.getGroupId();

        Properties p = new Properties();

        p.setProperty( "groupId", project.getGroupId() );

        p.setProperty( "artifactId", project.getArtifactId() );

        p.setProperty( "version", project.getVersion() );
        
        System.out.println("debugFilename: "+debugFilename);
        String[] splitName = debugFilename.split("\\.");
        String artifactSpecificName = "";
    	for(int i=0; i<splitName.length; i++) {
    		
    		if(i == splitName.length - 1) {
    			artifactSpecificName += "["+project.getArtifactId()+"].";
    		}
    		
    		artifactSpecificName += splitName[i];
    	}
        
        p.setProperty( "debugFilename", artifactSpecificName);

        createPropertyFile( p, pomPropertiesFile, forceCreation );

        archiver.addFile( pomPropertiesFile, "META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties" );
    }
	
}
