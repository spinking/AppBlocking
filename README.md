# AppBlocking

Android library for protecting the vendor when publishing the application in the store.

## Implementation

Step 1. Add the JitPack repository to your build file. Add it in your root build.gradle at the end of repositories:

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency

```java
	dependencies {
	        implementation 'com.github.spinking:AppBlocking:0.1.7'
	}
```
## How to use

Add builder where is it necessary

```kotlin        
BlockingBuilder()                                           // Use builder to configure end point. Response should be Boolean
    .setBaseUrl("https://expenses.profsoft.online/")        // Set base url, that using in Retrofit.Builder()
    .setEndPoint("project/external-status/finnflar")        // Set relative URL. It's @Url annotation for API method
    .blockingRequest(this)                                  // Start check
```
            
## License

AppBlocking is distributed under the MIT license. See [LICENSE](https://github.com/spinking/AppBlocking/blob/master/LICENSE.md) for details.
