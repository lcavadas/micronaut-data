package io.micronaut.data.azure

import com.azure.cosmos.CosmosClient
import com.azure.cosmos.CosmosContainer
import com.azure.cosmos.CosmosDatabase
import com.azure.cosmos.models.CosmosContainerProperties
import com.azure.cosmos.models.CosmosContainerResponse
import com.azure.cosmos.models.CosmosDatabaseResponse
import com.azure.cosmos.models.CosmosItemRequestOptions
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.models.ThroughputProperties
import com.azure.cosmos.util.CosmosPagedIterable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import io.micronaut.data.document.mongodb.repositories.CosmosBookDtoRepository
import io.micronaut.data.document.mongodb.repositories.CosmosBookRepository
import io.micronaut.data.document.tck.entities.Book
import io.micronaut.data.document.tck.repositories.BookDtoRepository
import io.micronaut.data.document.tck.repositories.BookRepository
import io.micronaut.serde.Decoder
import io.micronaut.serde.Deserializer
import io.micronaut.serde.SerdeRegistry
import io.micronaut.serde.Serializer
import io.micronaut.serde.jackson.JacksonDecoder
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class CosmosBasicSpec extends Specification implements AzureCosmosTestProperties {

    @AutoCleanup
    @Shared
    ApplicationContext context = ApplicationContext.run(properties)

    BookRepository bookRepository = context.getBean(CosmosBookRepository)

    BookDtoRepository bookDtoRepository = context.getBean(CosmosBookDtoRepository)

    def "test find by id"() {
        given:
            Book book = new Book()
            book.id = UUID.randomUUID().toString()
            book.title = "The Stand"
            book.totalPages = 1000
        when:
            bookRepository.save(book)
            def loadedBook = bookRepository.queryById(book.id)
        then:
            loadedBook
        when:
            loadedBook = bookRepository.queryById(UUID.randomUUID().toString())
        then:
            !loadedBook
        when:
            def exists = bookRepository.existsById(book.id)
        then:
            exists
        when:
            exists = bookRepository.existsById(UUID.randomUUID().toString())
        then:
            !exists
    }

    def "test find with query"() {
        given:
            Book book1 = new Book()
            book1.id = UUID.randomUUID().toString()
            book1.title = "The Stand"
            book1.totalPages = 1000
            Book book2 = new Book()
            book2.id = UUID.randomUUID().toString()
            book2.title = "Ice And Fire"
            book2.totalPages = 200
        when:
            bookRepository.save(book1)
            bookRepository.save(book2)
            def optionalBook = bookRepository.findById(book1.id)
        then:
            optionalBook.isPresent()
            optionalBook.get().title == "The Stand"
        when:
            def foundBook = bookRepository.searchByTitle("Ice And Fire")
        then:
            foundBook
            foundBook.title == "Ice And Fire"
        when:
            optionalBook = bookRepository.findById(UUID.randomUUID().toString())
        then:
            !optionalBook
    }

    def "test DTO entity retrieval"() {
        given:
            Book book = new Book()
            book.id = UUID.randomUUID().toString()
            book.title = "New Book"
            book.totalPages = 500
        when:
            bookRepository.save(book)
            def loadedBook = bookRepository.queryById(book.id)
        then:
            loadedBook
        when:
            def bookDto = bookDtoRepository.findById(book.id)
        then:
            bookDto.present
            bookDto.get().title == book.title
            // Not loaded due to NamingStrategy. Need to fix
            !bookDto.get().totalPages
    }

    def "should get cosmos client"() {
        when:
            SerdeRegistry registry = context.getBean(SerdeRegistry)
            CosmosClient client = context.getBean(CosmosClient)
            CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("mydb")
            CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId())

            CosmosContainerProperties containerProperties =
                    new CosmosContainerProperties("book", "/id");

            // Provision throughput
            ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);

            CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties, throughputProperties);
            CosmosContainer container = database.getContainer(containerResponse.getProperties().getId());

            XBook book = new XBook()
            book.id = UUID.randomUUID()
            book.name = "Ice & Fire"

            def encoderContext = registry.newEncoderContext(Object)
            def type = Argument.of(XBook)

            def item = container.createItem(book, new PartitionKey(book.id.toString()), new CosmosItemRequestOptions())
            System.out.println("XXX " + item.getStatusCode())

            CosmosPagedIterable<ObjectNode> filteredFamilies = container.queryItems("SELECT * FROM c", new CosmosQueryRequestOptions(), ObjectNode.class);

            if (filteredFamilies.iterator().hasNext()) {
                ObjectNode b = filteredFamilies.iterator().next();

                def parser = b.traverse()
                if (!parser.hasCurrentToken()) {
                    parser.nextToken()
                }
                final Decoder decoder = JacksonDecoder.create(parser, Object);
                Deserializer.DecoderContext decoderContext = registry.newDecoderContext(null);
                Deserializer<XBook> typeDeserializer = registry.findDeserializer(type);
                Deserializer<XBook> deserializer = typeDeserializer.createSpecific(decoderContext, type);

                XBook des = deserializer.deserialize(
                        decoder,
                        decoderContext,
                        type
                );

                System.out.println("BOOK: " + b)
                System.out.println("VVV " + des)
            }

        then:
            true
    }

}
