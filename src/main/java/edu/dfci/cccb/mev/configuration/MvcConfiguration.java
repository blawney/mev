/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.dfci.cccb.mev.configuration;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.math3.linear.RealMatrix;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import edu.dfci.cccb.mev.beans.Matrices;

/**
 * @author levk
 * 
 */
@Configuration
@EnableWebMvc
@ComponentScan ("edu.dfci.cccb.mev")
@Log4j
public class MvcConfiguration extends WebMvcConfigurerAdapter {

  /**
   * Creates the session scoped matrix holding bean
   * 
   * @return
   */
  // TODO: Move this out to a different config file to prepare to separate
  // heatmaps into its own module
  @Bean
  @Scope (value = SCOPE_SESSION, proxyMode = TARGET_CLASS)
  public Matrices matrices () {
    return new Matrices ();
  }

  /**
   * Create the CNVR. Get Spring to inject the ContentNegotiationManager created
   * by the configurer (see previous method).
   */
  @Bean
  public ViewResolver contentNegotiatingViewResolver (final ContentNegotiationManager manager) {
    return new ContentNegotiatingViewResolver () {
      {
        setContentNegotiationManager (manager);
      }
    };
  }

  /**
   * Multipart resolver for file upload
   * 
   * @return
   */
  @Bean
  public CommonsMultipartResolver multipartResolver () {
    return new CommonsMultipartResolver () {
      {
        setMaxUploadSize (10000000);
      }
    };
  }

  @Bean (name = "buildProperties")
  public PropertiesFactoryBean globalBuildProperties () {
    return new PropertiesFactoryBean () {
      {
        setLocation (new ClassPathResource ("build.properties"));
      }
    };
  }

  @Bean (name = "jspViewResolver")
  public ViewResolver jspViewResolver () {
    return new InternalResourceViewResolver () {
      {
        setPrefix ("META-INF/resources/mev/views/");
        setSuffix (".jsp");
        setOrder (2);
        setExposedContextBeanNames (new String[] { "buildProperties" });
      }
    };
  }

  @Bean (name = "jsonViewResolver")
  public ViewResolver jsonViewResolver () {
    return new ViewResolver () {

      @Override
      public View resolveViewName (String viewName, Locale locale) throws Exception {
        return new MappingJackson2JsonView () {
          {
            setPrettyPrint (true);
          }
        };
      }
    };
  }

  // TODO: This should go into a separate config for heatmaps
  @Bean (name = "tsvViewResolver")
  public ViewResolver tsvViewResolver () {
    return new ViewResolver () {

      @Override
      public View resolveViewName (String viewName, Locale locale) throws Exception {
        return new AbstractView () {

          @Override
          protected void renderMergedOutputModel (Map<String, Object> model,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) throws Exception {
            PrintStream out = new PrintStream (response.getOutputStream ());
            for (Entry<String, Object> entry : model.entrySet ())
              if (entry.getValue () instanceof RealMatrix) {
                RealMatrix matrix = (RealMatrix) entry.getValue ();
                for (int column = 0, columns = matrix.getColumnDimension (); column < columns; column++)
                  for (int row = 0, rows = matrix.getRowDimension (); row < rows; row++)
                    out.print (matrix.getEntry (row, column) + (column == columns - 1 ? "\n" : "\t"));
                return;
              }
          }
        };
      }
    };
  }

  /* (non-Javadoc)
   * @see
   * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
   * #configureContentNegotiation
   * (org.springframework.web.servlet.config.annotation
   * .ContentNegotiationConfigurer) */
  @Override
  public void configureContentNegotiation (ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension (true)
              .favorParameter (true)
              .parameterName ("format")
              .ignoreAcceptHeader (true)
              .useJaf (false)
              .defaultContentType (TEXT_HTML)
              .mediaType ("html", TEXT_HTML)
              .mediaType ("xml", APPLICATION_XML)
              .mediaType ("tsv", TEXT_PLAIN)
              .mediaType ("json", APPLICATION_JSON);
  }

  /* (non-Javadoc)
   * @see
   * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
   * #configureMessageConverters(java.util.List) */
  // TODO: This should go into a separate config for heatmaps
  /* @Override public void configureMessageConverters
   * (List<HttpMessageConverter<?>> converters) { converters.add (new
   * MappingJackson2HttpMessageConverter () { { setObjectMapper (new
   * ObjectMapper ().registerModule (new SimpleModule () { private static final
   * long serialVersionUID = 1L; { addSerializer (RealMatrix.class, new
   * JsonSerializer<RealMatrix> () {
   * @Override public void serialize (RealMatrix value, JsonGenerator jgen,
   * SerializerProvider provider) throws IOException, JsonProcessingException {
   * if (log.isDebugEnabled ()) log.debug ("Serializing matrix " + value +
   * " to JSON"); jgen.writeStartObject (); jgen.writeNumberField ("rows", value
   * == null ? 0 : value.getRowDimension ()); jgen.writeNumberField ("columns",
   * value == null ? 0 : value.getColumnDimension ()); jgen.writeArrayFieldStart
   * ("data"); if (value != null) { for (int column = 0; column <
   * value.getColumnDimension (); column++) for (int row = 0; row <
   * value.getRowDimension (); row++) jgen.writeNumber (value.getEntry (row,
   * column)); jgen.writeEndArray (); } jgen.writeEndObject (); }
   * @Override public Class<RealMatrix> handledType () { return
   * RealMatrix.class; } }); } })); } }); } /* (non-Javadoc)
   * @see
   * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
   * #addResourceHandlers
   * (org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry) */
  @Override
  public void addResourceHandlers (ResourceHandlerRegistry registry) {
    registry.addResourceHandler ("/resources/static/**").addResourceLocations ("classpath:/META-INF/resources/",
                                                                               "/META-INF/resources/");
  }
}
