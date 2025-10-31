package dev.fernandocortez.humblebundlelibrarydownloader.resources;

import dev.fernandocortez.humblebundlelibrarydownloader.dto.HumbleBundleLibraryEbook;
import dev.fernandocortez.humblebundlelibrarydownloader.repositories.HumbleBundleLibraryRepository;
import dev.fernandocortez.humblebundlelibrarydownloader.services.HumbleBundlePlaywrightBrowserService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Path("/ebooks")
public class EbooksResource {

  @Inject
  HumbleBundleLibraryRepository humbleBundleLibraryRepository;

  @Inject
  HumbleBundlePlaywrightBrowserService browserService;

  @CheckedTemplate
  public static class Templates {

    public static native TemplateInstance ebooks(List<HumbleBundleLibraryEbook> ebooks);
  }

  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/load")
  public Response loadAllEbooks() {
//    browserService.populateDatabase();
    return Response.ok("Loading...").build();
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAllEbooks() {
    final List<HumbleBundleLibraryEbook> allEbooks = humbleBundleLibraryRepository.selectAllEbooks();
    return Templates.ebooks(allEbooks);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/download")
  public Response downloadSelectedBooks(Set<Long> fileIds) {
    // browserService.downloadSelectedFiles(fileIds);
    return Response.accepted("Downloading").build();
  }

}
