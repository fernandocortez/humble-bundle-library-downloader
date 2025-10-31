package dev.fernandocortez.humblebundlelibrarydownloader.resources;

import dev.fernandocortez.humblebundlelibrarydownloader.dto.HumbleBundleLibraryEbook;
import dev.fernandocortez.humblebundlelibrarydownloader.repositories.HumbleBundleLibraryRepository;
import dev.fernandocortez.humblebundlelibrarydownloader.services.HumbleBundlePlaywrightBrowserService;
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

@Path("/api/ebooks")
@Produces(MediaType.APPLICATION_JSON)
public class EbookResource {

  @Inject
  HumbleBundleLibraryRepository humbleBundleLibraryRepository;

  @Inject
  HumbleBundlePlaywrightBrowserService browserService;

  @POST
  @Path("/load")
  public Response loadAllEbooks() {
    browserService.populateDatabase();
    return Response.ok().build();
  }

  @GET
  public List<HumbleBundleLibraryEbook> getAllEbooks() {
    return humbleBundleLibraryRepository.selectAllEbooks();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/download")
  public Response downloadSelectedBooks(Set<Long> fileIds) {
    browserService.downloadSelectedFiles(fileIds);
    return Response.accepted().build();
  }

}
